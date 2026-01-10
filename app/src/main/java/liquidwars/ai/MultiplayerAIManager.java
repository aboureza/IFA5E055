package liquidwars.ai;

import liquidwars.model.Particle;
import liquidwars.model.World;
import liquidwars.ui.MultiplayerGameController;

import javax.swing.Timer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Manages AI for 3 bot players in multiplayer mode.
 * Three behaviors:
 * - MIRROR_PLAYER: mirrors the player's (team 0) target with periodic randomization
 * - ATTACK_CLOSEST: targets the closest enemy team's center with periodic randomization
 * - MIRROR_ATTACKER: mirrors the ATTACK_CLOSEST bot's target with periodic randomization
 * 
 * Roles are randomly assigned at initialization to add variety.
 * All behaviors include random target pickups at configurable intervals for liveliness.
 * Isolated from 2-team AI to maintain OOP separation.
 */
public final class MultiplayerAIManager {

    private enum Behavior { MIRROR_PLAYER, ATTACK_CLOSEST, MIRROR_ATTACKER }

    private final MultiplayerGameController controller;
    private final OpponentAI mirrorAI;
    private final Timer timer;
    private final boolean[][] walls;
    private final int width;
    private final int height;
    private final Random rnd;

    // Team assignments (1, 2, 3) -> behavior
    private final Behavior team1Behavior;
    private final Behavior team2Behavior;
    private final Behavior team3Behavior;

    // Random target management for each team
    private OpponentAI.Target[] randomTargets = new OpponentAI.Target[4];
    private long[] randomExpiryTimes = new long[4];
    private long[] lastRandomizeTimes = new long[4];

    // Intervals for random target picking
    private static final long ATTACKER_INTERVAL_MS = 3000L;     // attackers pick random every 3s (more often)
    private static final long RANDOM_DURATION_MS = 2000L;       // random target lasts 2s
    
    // Stagger offsets so each team randomizes at different times
    private static final long STAGGER_OFFSET_MS = 1000L;         // 1 second offset between each team

    // Store attacker target for mirroring
    private int attackerTargetX;
    private int attackerTargetY;

    public MultiplayerAIManager(MultiplayerGameController controller, boolean[][] walls, int width, int height) {
        this.controller = controller;
        this.walls = walls;
        this.width = width;
        this.height = height;
        this.rnd = new Random();
        this.mirrorAI = new OpponentAI(walls);

        // Randomly assign behaviors to teams 1, 2, 3
        List<Behavior> behaviors = new ArrayList<>();
        behaviors.add(Behavior.MIRROR_PLAYER);
        behaviors.add(Behavior.ATTACK_CLOSEST);
        behaviors.add(Behavior.MIRROR_ATTACKER);
        Collections.shuffle(behaviors, rnd);

        this.team1Behavior = behaviors.get(0);
        this.team2Behavior = behaviors.get(1);
        this.team3Behavior = behaviors.get(2);

        // Initialize random target tracking with staggered offsets
        long now = System.currentTimeMillis();
        for (int i = 0; i < 4; i++) {
            randomTargets[i] = null;
            randomExpiryTimes[i] = Long.MIN_VALUE;
            // Stagger each team's randomization by STAGGER_OFFSET_MS
            lastRandomizeTimes[i] = now - (i * STAGGER_OFFSET_MS);
        }

        // Initialize attacker target to center
        this.attackerTargetX = width / 2;
        this.attackerTargetY = height / 2;

        // Timer to update AI every 100ms
        this.timer = new Timer(100, e -> updateOnce());
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    private void updateOnce() {
        // Get player's target (team 0)
        int playerX = controller.getTargetX(0);
        int playerY = controller.getTargetY(0);

        // Update each bot team based on its assigned behavior
        updateTeam(1, team1Behavior, playerX, playerY);
        updateTeam(2, team2Behavior, playerX, playerY);
        updateTeam(3, team3Behavior, playerX, playerY);
    }

    private void updateTeam(int teamId, Behavior behavior, int playerX, int playerY) {
        switch (behavior) {
            case MIRROR_PLAYER -> {
                // Mirror the player's target with randomization (via OpponentAI)
                OpponentAI.Target t = mirrorAI.nextTarget(playerX, playerY);
                controller.setTarget(teamId, t.x(), t.y());
            }
            case ATTACK_CLOSEST -> {
                // Attack closest enemy with periodic random diversification
                OpponentAI.Target target = getTargetWithRandomization(teamId, ATTACKER_INTERVAL_MS);
                if (target == null) {
                    target = findClosestEnemyCenter(teamId);
                }
                controller.setTarget(teamId, target.x(), target.y());
                // Store for MIRROR_ATTACKER to use
                attackerTargetX = target.x();
                attackerTargetY = target.y();
            }
            case MIRROR_ATTACKER -> {
                // Mirror the attacker's target with periodic random diversification
                OpponentAI.Target target = getTargetWithRandomization(teamId, ATTACKER_INTERVAL_MS);
                if (target == null) {
                    int mx = width - 1 - attackerTargetX;
                    int my = attackerTargetY;
                    mx = clamp(mx, 0, width - 1);
                    my = clamp(my, 0, height - 1);
                    if (walls[my][mx]) {
                        mx = attackerTargetX;
                        my = attackerTargetY;
                    }
                    target = new OpponentAI.Target(mx, my);
                }
                controller.setTarget(teamId, target.x(), target.y());
            }
        }
    }

    /**
     * Handle random target picking for a team.
     * If a random target is active, return it.
     * If randomization interval has passed, pick a new random target.
     * Otherwise, return null to use default behavior.
     */
    private OpponentAI.Target getTargetWithRandomization(int teamId, long intervalMs) {
        long now = System.currentTimeMillis();

        // If a random target is currently active and not expired, keep using it
        if (randomTargets[teamId] != null && now < randomExpiryTimes[teamId]) {
            return randomTargets[teamId];
        }

        // If the active random target expired, clear it
        if (randomTargets[teamId] != null && now >= randomExpiryTimes[teamId]) {
            randomTargets[teamId] = null;
        }

        // Time to pick a new random target?
        if (now - lastRandomizeTimes[teamId] >= intervalMs) {
            OpponentAI.Target r = chooseRandomFreeCell();
            if (r != null) {
                lastRandomizeTimes[teamId] = now;
                randomTargets[teamId] = r;
                randomExpiryTimes[teamId] = now + RANDOM_DURATION_MS;
                return r;
            }
        }

        return null;
    }

    /**
     * Find the center of mass of the closest enemy team (by particle count proximity)
     */
    private OpponentAI.Target findClosestEnemyCenter(int myTeamId) {
        World world = controller.getWorld();
        
        // Calculate center of mass for each enemy team
        int[] enemyTeams = {0, 1, 2, 3};
        double closestDistance = Double.MAX_VALUE;
        int closestEnemyTeam = -1;

        // Get my team's center first
        OpponentAI.Target myCenter = getTeamCenter(myTeamId, world);

        for (int enemyTeam : enemyTeams) {
            if (enemyTeam == myTeamId) continue;

            OpponentAI.Target enemyCenter = getTeamCenter(enemyTeam, world);
            if (enemyCenter == null) continue;

            double dist = distance(myCenter.x(), myCenter.y(), enemyCenter.x(), enemyCenter.y());
            if (dist < closestDistance) {
                closestDistance = dist;
                closestEnemyTeam = enemyTeam;
            }
        }

        if (closestEnemyTeam == -1) {
            // No valid enemy found, return center of map
            return new OpponentAI.Target(width / 2, height / 2);
        }

        return getTeamCenter(closestEnemyTeam, world);
    }

    /**
     * Calculate center of mass for a team's particles
     */
    private OpponentAI.Target getTeamCenter(int teamId, World world) {
        long sumX = 0;
        long sumY = 0;
        int count = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Particle p = world.get(x, y);
                if (p != null && p.teamId() == teamId) {
                    sumX += x;
                    sumY += y;
                    count++;
                }
            }
        }

        if (count == 0) {
            // No particles for this team, return center of map
            return new OpponentAI.Target(width / 2, height / 2);
        }

        int centerX = (int) (sumX / count);
        int centerY = (int) (sumY / count);

        return new OpponentAI.Target(
            clamp(centerX, 0, width - 1),
            clamp(centerY, 0, height - 1)
        );
    }

    /**
     * Pick a random free cell
     */
    private OpponentAI.Target chooseRandomFreeCell() {
        List<OpponentAI.Target> free = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!walls[y][x]) {
                    free.add(new OpponentAI.Target(x, y));
                }
            }
        }
        if (free.isEmpty()) return null;
        return free.get(rnd.nextInt(free.size()));
    }

    private double distance(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private int clamp(int v, int lo, int hi) {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }
}
