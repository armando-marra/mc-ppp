"""Local release regression checks: no network calls or real publications."""

import contextlib
import importlib.util
import json
import os
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
import zipfile


spec = importlib.util.spec_from_file_location("release", Path(__file__).with_name("release.py"))
release = importlib.util.module_from_spec(spec)
spec.loader.exec_module(release)


class ReleaseTests(unittest.TestCase):
    @contextlib.contextmanager
    def fixture(self):
        original = Path.cwd()
        with tempfile.TemporaryDirectory() as directory:
            os.chdir(directory)
            try:
                Path("gradle.properties").write_text("mod_version=1.2.3\narchives_base_name=pointportals\n")
                Path("CHANGELOG.md").write_text("## [Unreleased]\n\nFuture\n\n## [1.2.3]\n\nRelease notes\n\n---\n\n## [1.2.2]\n\nOld\n")
                with patch.dict(os.environ, GITHUB_REF_NAME="v1.2.3", GITHUB_REPOSITORY="owner/repo"):
                    yield
            finally:
                os.chdir(original)

    def write_jars(self, runtime_version="1.2.3", extra=None):
        Path("build/libs").mkdir(parents=True, exist_ok=True)
        for suffix, version in (("", runtime_version), ("-sources", "${version}")):
            with zipfile.ZipFile(f"build/libs/pointportals-1.2.3{suffix}.jar", "w") as jar:
                jar.writestr("fabric.mod.json", json.dumps({"id": "pointportals", "version": version}))
                if extra:
                    jar.writestr(extra, "unexpected")

    def test_versions_and_notes(self):
        for tag in ("v1.2.3", "v1.2.3-rc.1"):
            self.assertEqual(release.version_from_tag(tag), tag[1:])
        for tag in ("1.2.3", "v01.2.3", "v1.2.3-rc.0", "v1.2.3-extra", "v1.2.3\n"):
            with self.assertRaises(ValueError):
                release.version_from_tag(tag)
        with self.fixture():
            self.assertEqual(release.release_notes("1.2.3"), "Release notes\n")
            with self.assertRaises(ValueError):
                release.release_notes("1.2.4")

    def test_prepare_version_branch_and_existing_release(self):
        with self.fixture(), patch.object(release, "run", return_value="commit"), \
                patch.object(release, "releases", return_value=[]), patch.object(release.subprocess, "run") as git:
            release.prepare()
            self.assertEqual(git.call_args.args[0][-1], "refs/remotes/origin/main")
            with patch.dict(os.environ, GITHUB_REF_NAME="v1.2.4"):
                with self.assertRaises(ValueError):
                    release.prepare()
            with patch.object(release, "releases", return_value=[{"tag_name": "v1.2.3"}]):
                with self.assertRaises(ValueError):
                    release.prepare()

    def test_package_checks_metadata_and_excludes_tests(self):
        with self.fixture():
            self.write_jars()
            assets = release.package()
            self.assertEqual(len(assets), 3)
            self.assertEqual(len(assets[-1].read_text().splitlines()), 2)
            for bad_version in ("0.0.0", "${version}"):
                self.write_jars(runtime_version=bad_version)
                with self.assertRaises(ValueError):
                    release.package()
            self.write_jars(extra="com/project/pointportals/benchmark/LoadingBenchmark.class")
            with self.assertRaises(ValueError):
                release.package()

    def test_latest_uses_numeric_versions_and_excludes_rc(self):
        existing = [{"tag_name": "v1.10.0", "draft": False, "prerelease": False}]
        self.assertFalse(release.latest_stable("1.9.0", existing))
        self.assertTrue(release.latest_stable("1.11.0", existing))
        self.assertFalse(release.latest_stable("2.0.0-rc.1", existing))

    def test_failed_upload_verification_never_publishes(self):
        with self.fixture(), patch.object(release, "releases", return_value=[]), \
                patch.object(release, "run", return_value='{"assets": []}'), \
                patch.object(release.subprocess, "run") as gh:
            self.write_jars()
            with self.assertRaises(ValueError):
                release.publish()
            self.assertEqual(gh.call_count, 1)
            self.assertEqual(gh.call_args.args[0][1:3], ["release", "create"])
            self.assertIn("--draft", gh.call_args.args[0])

    def test_successful_publication(self):
        with self.fixture(), patch.object(release, "releases", return_value=[]), \
                patch.object(release.subprocess, "run") as gh:
            self.write_jars()
            assets = release.package()
            uploaded = {"assets": [{"name": asset.name, "size": asset.stat().st_size} for asset in assets]}
            with patch.object(release, "run", return_value=json.dumps(uploaded)):
                release.publish()
            self.assertEqual(gh.call_count, 2)
            self.assertIn("--draft=false", gh.call_args.args[0])
            self.assertIn("--latest=true", gh.call_args.args[0])


if __name__ == "__main__":
    unittest.main()
