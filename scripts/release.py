"""Small release checks and publishing helpers; Python standard library only."""

import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import sys
import zipfile


VERSION = r"(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)(?:-rc\.([1-9][0-9]*))?"
OUTPUT = Path("build/release")


def run(*args):
    return subprocess.check_output(args, text=True, encoding="utf-8").strip()


def properties():
    return dict(re.findall(r"^([\w.]+)\s*=\s*(.*?)\s*$", Path("gradle.properties").read_text(), re.M))


def version_from_tag(tag):
    if not re.fullmatch("v" + VERSION, tag):
        raise ValueError(f"Invalid tag: {tag}; expected vMAJOR.MINOR.PATCH or vMAJOR.MINOR.PATCH-rc.N")
    return tag[1:]


def release_notes(version):
    changelog = Path("CHANGELOG.md").read_text(encoding="utf-8")
    sections = re.findall(
        rf"^## \[{re.escape(version)}\][^\n]*\n(.*?)(?=^## |\Z)", changelog, re.M | re.S
    )
    if len(sections) != 1:
        raise ValueError(f"Expected exactly one CHANGELOG.md section for {version}")
    notes = re.sub(r"\n---\s*$", "", sections[0]).strip()
    if not notes:
        raise ValueError(f"Empty changelog section for {version}")
    return notes + "\n"


def releases():
    pages = json.loads(run("gh", "api", "--paginate", "--slurp",
                           f"repos/{os.environ['GITHUB_REPOSITORY']}/releases?per_page=100"))
    return [release for page in pages for release in page]


def ensure_new(tag, existing):
    if any(release["tag_name"] == tag for release in existing):
        raise ValueError(f"Release {tag} already exists (possibly a draft). Inspect it manually before retrying; nothing will be overwritten.")


def prepare():
    tag = os.environ["GITHUB_REF_NAME"]
    version = version_from_tag(tag)
    if properties()["mod_version"] != version:
        raise ValueError("Tag version differs from mod_version in gradle.properties")
    commit = run("git", "rev-parse", "HEAD")
    if run("git", "rev-parse", f"refs/tags/{tag}^{{commit}}") != commit:
        raise ValueError("Checkout does not match the tagged commit")
    branch = "devel" if "-rc." in version else "main"
    subprocess.run(["git", "merge-base", "--is-ancestor", commit, f"refs/remotes/origin/{branch}"], check=True)
    notes = release_notes(version)
    ensure_new(tag, releases())
    OUTPUT.mkdir(parents=True, exist_ok=True)
    (OUTPUT / "notes.md").write_text(notes, encoding="utf-8")
    print(f"Validated {tag} at {commit} on {branch}")


def package():
    config = properties()
    version = version_from_tag(os.environ["GITHUB_REF_NAME"])
    if config["mod_version"] != version:
        raise ValueError("Tag version differs from mod_version")
    jars = [Path("build/libs") / f"{config['archives_base_name']}-{version}{suffix}.jar"
            for suffix in ("", "-sources")]
    for jar in jars:
        with zipfile.ZipFile(jar) as archive:
            if archive.testzip() is not None:
                raise ValueError(f"Corrupt JAR: {jar}")
            metadata = json.loads(archive.read("fabric.mod.json"))
            # sourcesJar includes original resources, before processResources expansion.
            versions = {version, "${version}"} if jar == jars[1] else {version}
            if metadata["id"] != "pointportals" or metadata["version"] not in versions:
                raise ValueError(f"Incorrect mod metadata in {jar}")
            if any("gametest" in name.lower() or "benchmark" in name.lower()
                   for name in archive.namelist()):
                raise ValueError(f"Test/benchmark content found in {jar}")
    OUTPUT.mkdir(parents=True, exist_ok=True)
    checksums = "".join(f"{hashlib.sha256(jar.read_bytes()).hexdigest()}  {jar.name}\n" for jar in jars)
    (OUTPUT / "SHA256SUMS.txt").write_text(checksums, encoding="utf-8")
    return jars + [OUTPUT / "SHA256SUMS.txt"]


def latest_stable(version, existing):
    if "-rc." in version:
        return False
    current = tuple(map(int, version.split(".")))
    for release in existing:
        match = re.fullmatch(r"v(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)", release["tag_name"])
        if match and not release["draft"] and not release["prerelease"]:
            if tuple(map(int, match.groups())) > current:
                return False
    return True


def publish():
    tag = os.environ["GITHUB_REF_NAME"]
    version = version_from_tag(tag)
    existing = releases()
    ensure_new(tag, existing)
    assets = package()
    # Explicit repository avoids dependence on checkout credentials or gh defaults.
    repo_args = ["--repo", os.environ["GITHUB_REPOSITORY"]]
    command = ["gh", "release", "create", tag, *repo_args, "--verify-tag", "--draft",
               "--title", tag, "--notes-file", str(OUTPUT / "notes.md")]
    if "-rc." in version:
        command.append("--prerelease")
    subprocess.run(command + [str(asset) for asset in assets], check=True)
    uploaded = json.loads(run("gh", "release", "view", tag, *repo_args, "--json", "assets"))["assets"]
    expected = {asset.name: asset.stat().st_size for asset in assets}
    if len(uploaded) != len(expected) or {asset["name"]: asset["size"] for asset in uploaded} != expected:
        raise ValueError("Uploaded assets do not match; release remains a draft")
    # Re-read other releases immediately before deciding whether to promote Latest.
    latest = "true" if latest_stable(version, releases()) else "false"
    subprocess.run(["gh", "release", "edit", tag, *repo_args, "--draft=false", f"--latest={latest}"], check=True)


if __name__ == "__main__":
    try:
        {"prepare": prepare, "package": package, "publish": publish}[sys.argv[1]]()
    except (ValueError, KeyError, OSError, subprocess.CalledProcessError, zipfile.BadZipFile) as error:
        sys.exit(f"Release failed: {error}")
