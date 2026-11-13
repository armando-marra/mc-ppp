package com.project.pointportals.benchmark;

import com.project.pointportals.PointPortals;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;

/** Selected instead of the regression entrypoints only by -PportalBenchmark. */
public final class LoadingBenchmark {
	@GameTest(maxTicks = 2_000_000)
	public void controlledChunkLoads(GameTestHelper test) throws IOException {
		new Campaign(test).start();
	}

	private record Cell(String operation, String state, boolean nether, int repeat) {
		String dimension() { return nether ? "minecraft:the_nether" : "minecraft:overworld"; }
	}

	private static final class Campaign {
		final GameTestHelper test;
		final MinecraftServer server;
		final int samples = setting("samples", 30, 1, 100);
		final int burst = setting("burst", 10, 1, 30);
		final int repeats = setting("repeats", 3, 1, 10);
		final int interval = setting("interval", 20, 1, 100);
		final int warmup = setting("warmupTicks", 1200, 0, 10000);
		final List<Cell> cells = new ArrayList<>();
		final List<BenchmarkFixture> fixtures = new ArrayList<>();
		final List<Pending> checks = new ArrayList<>();
		final PrintWriter operations;
		final PrintWriter ticks;
		final PrintWriter lifecycle;
		String stage = "warmup";
		int cellIndex;
		long stageAt;
		int nextSample;
		int unique;
		Tick pendingTick;
		boolean done;

		Campaign(GameTestHelper test) throws IOException {
			this.test = test;
			server = test.getLevel().getServer();
			operations = new PrintWriter(Files.newBufferedWriter(Path.of("benchmark-operations.csv")));
			ticks = new PrintWriter(Files.newBufferedWriter(Path.of("benchmark-ticks.csv")));
			lifecycle = new PrintWriter(Files.newBufferedWriter(Path.of("benchmark-lifecycle.csv")));
			operations.println("Cell,Operation,State,Dimension,Repeat,Mode,Sample,Tick,ChunkX,ChunkZ,LoadedBefore,Success,Verified,DurationNs,VerificationDelayTicks");
			ticks.println("Cell,Operation,State,Dimension,Repeat,Mode,Tick,Operations,DurationNs");
			lifecycle.println("Cell,Targets,ReleaseCheckTicks,AllUnloaded");
			for (int repeat = 1; repeat <= repeats; repeat++) {
				for (boolean nether : new boolean[]{false, true}) {
					for (String operation : List.of("tuner", "teleport")) {
						for (String state : List.of("loaded", "unloaded", "new")) {
							cells.add(new Cell(operation, state, nether, repeat));
						}
					}
				}
			}
			String filter = System.getProperty("pointportals.benchmark.filter", ".*");
			cells.removeIf(c -> !(c.operation + ":" + c.state + ":" + c.dimension()).matches(filter));
			if (cells.isEmpty()) throw new IllegalArgumentException("No benchmark cells match " + filter);
			Files.writeString(Path.of("benchmark-runtime.txt"), "seed=" + test.getLevel().getSeed()
				+ "\njava=" + System.getProperty("java.runtime.version") + "\nvm=" + System.getProperty("java.vm.name")
				+ "\nheapMax=" + Runtime.getRuntime().maxMemory() + "\nprocessors=" + Runtime.getRuntime().availableProcessors()
				+ "\nos=" + System.getProperty("os.name") + " " + System.getProperty("os.version")
				+ "\nrunner=unpaced GameTest server; vanilla completed tick work times, not 20 TPS wall pacing"
				+ "\nsamples=" + samples + "\nburst=" + burst + "\nrepeats=" + repeats
				+ "\nserialIntervalTicks=" + interval + "\nwarmupTicks=" + warmup + "\ncells=" + cells.size() + "\nfilter=" + filter + "\n");
		}

		void start() {
			stageAt = test.getTick();
			test.onEachTick(() -> {
				if (done) return;
				try { step(); }
				catch (RuntimeException | Error failure) { close(); throw failure; }
			});
			test.runAfterDelay(1_999_900, () -> { if (!done) { close(); test.fail("Benchmark timed out"); } });
		}

		void step() {
			collectTick();
			verifyPending();
			switch (stage) {
				case "warmup" -> { if (test.getTick() - stageAt >= warmup) stage = "prepare"; }
				case "prepare" -> prepare();
				case "wait" -> {
					boolean loaded = cell().state.equals("loaded");
					if (fixtures.stream().allMatch(f -> f.remote.isLoaded(f.target) == loaded)) {
						stage = "baseline"; stageAt = test.getTick();
					} else if (test.getTick() - stageAt > 800) test.fail("Fixture did not reach expected loaded state");
				}
				case "baseline" -> {
					markTick("baseline", 0);
					if (test.getTick() - stageAt >= 60) { stage = "serial"; nextSample = 0; stageAt = test.getTick() + 1; }
				}
				case "serial" -> {
					boolean act = nextSample < samples && (test.getTick() - stageAt) % interval == 0;
					markTick("serial", act ? 1 : 0);
					if (act) exercise(nextSample++, "serial");
					if (nextSample == samples && checks.isEmpty()) stage = "burst";
				}
				case "burst" -> {
					markTick("burst", burst);
					for (int i = 0; i < burst; i++) exercise(samples + i, "burst");
					stage = "settle";
				}
				case "settle" -> {
					markTick("burst", 0);
					if (checks.isEmpty()) stage = "cleanup";
				}
				case "cleanup" -> {
					for (var fixture : fixtures) fixture.close();
					operations.flush(); ticks.flush(); stage = "drain"; stageAt = test.getTick();
				}
				case "drain" -> {
					// Includes the vanilla arrival-ticket lifetime; no measured fixture may stay forced.
					if (test.getTick() - stageAt < 350) return;
					var held = fixtures.stream().filter(f -> f.remote.isLoaded(f.target)).toList();
					if (!held.isEmpty()) {
						if ((test.getTick() - stageAt) % 200 == 0) {
							PointPortals.LOGGER.info("Portal benchmark draining {} targets: {}", held.size(),
								held.getFirst().remote.getChunkSource().getChunkDebugData(
									net.minecraft.world.level.ChunkPos.containing(held.getFirst().target)));
						}
						if (test.getTick() - stageAt > 6000) test.fail("Fixture target still loaded after 6000 drain ticks");
						return;
					}
					long releaseTicks = test.getTick() - stageAt;
					lifecycle.printf("%d,%d,%d,true%n", cellIndex, fixtures.size(), releaseTicks);
					lifecycle.flush();
					PointPortals.LOGGER.info("Portal benchmark released cell {} after {} drain ticks", cellIndex + 1, releaseTicks);
					fixtures.clear();
					if (++cellIndex == cells.size()) { close(); test.succeed(); }
					else stage = "prepare";
				}
				default -> throw new IllegalStateException(stage);
			}
		}

		Cell cell() { return cells.get(cellIndex); }

		void prepare() {
			PointPortals.LOGGER.info("Portal benchmark cell {}/{}: {}", cellIndex + 1, cells.size(), cell());
			ServerLevel remote = server.getLevel(cell().nether ? Level.NETHER : Level.OVERWORLD);
			test.assertTrue(remote != null, "Benchmark dimension must exist");
			var player = test.makeMockPlayer(GameType.SURVIVAL);
			for (int i = 0; i < samples + burst; i++) {
				fixtures.add(new BenchmarkFixture(test, remote, player, cell().operation, cell().state, i, unique++));
			}
			for (var fixture : fixtures) fixture.prepareEntity(test);
			if (cell().state.equals("unloaded")) remote.getChunkSource().save(true);
			stage = "wait"; stageAt = test.getTick();
		}

		void exercise(int index, String mode) {
			var fixture = fixtures.get(index);
			boolean loaded = fixture.remote.isLoaded(fixture.target);
			test.assertTrue(loaded == cell().state.equals("loaded"), "Sample loaded state must match its cell");
			fixture.prepareEntity(test);
			long start = System.nanoTime();
			boolean success = fixture.exercise();
			long elapsed = System.nanoTime() - start;
			checks.add(new Pending(fixture, index, mode, server.getTickCount(), test.getTick() + 2, loaded, success, elapsed));
		}

		void verifyPending() {
			var iterator = checks.iterator();
			while (iterator.hasNext()) {
				Pending p = iterator.next();
				if (test.getTick() < p.verifyAt) continue;
				long delay = test.getTick() - p.verifyAt + 2;
				if (!p.fixture.arrivalReady() && delay < 200) continue;
				p.fixture.verify(test, p.success);
				operations.printf(java.util.Locale.ROOT, "%d,%s,%s,%s,%d,%s,%d,%d,%d,%d,%s,%s,true,%d,%d%n",
					cellIndex, cell().operation, cell().state, cell().dimension(), cell().repeat, p.mode, p.index,
					p.tick, p.fixture.target.getX() >> 4, p.fixture.target.getZ() >> 4, p.loaded, p.success, p.nanos, delay);
				if (p.fixture.pig != null) p.fixture.pig.discard();
				iterator.remove();
			}
		}

		void markTick(String mode, int count) {
			pendingTick = new Tick(server.getTickCount(), mode, count);
		}

		void collectTick() {
			if (pendingTick == null) return;
			test.assertTrue(server.getTickCount() == pendingTick.number + 1, "Must sample the immediately preceding completed tick");
			long nanos = server.getTickTimesNanos()[Math.floorMod(pendingTick.number, 100)];
			test.assertTrue(nanos > 0, "Completed tick must have a vanilla duration");
			ticks.printf(java.util.Locale.ROOT, "%d,%s,%s,%s,%d,%s,%d,%d,%d%n", cellIndex, cell().operation,
				cell().state, cell().dimension(), cell().repeat, pendingTick.mode, pendingTick.number, pendingTick.count, nanos);
			pendingTick = null;
		}

		void close() {
			done = true;
			try { for (var fixture : fixtures) fixture.close(); }
			finally { operations.close(); ticks.close(); lifecycle.close(); }
		}
	}

	private record Pending(BenchmarkFixture fixture, int index, String mode, int tick, long verifyAt,
		boolean loaded, boolean success, long nanos) { }
	private record Tick(int number, String mode, int count) { }

	private static int setting(String key, int fallback, int min, int max) {
		int value = Integer.getInteger("pointportals.benchmark." + key, fallback);
		if (value < min || value > max) throw new IllegalArgumentException("Invalid benchmark " + key);
		return value;
	}
}
