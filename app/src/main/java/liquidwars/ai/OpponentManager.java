package liquidwars.ai;

import liquidwars.ui.GameController;

import javax.swing.Timer;

/**
 * Runs the OpponentAI and updates team 1 target periodically.
 * Uses a Swing Timer so execution happens on the EDT (same as the UI loop).
 */
public final class OpponentManager {

    private final GameController controller;
    private final OpponentAI ai;
    private final Timer timer;

    public OpponentManager(GameController controller, OpponentAI ai, int tickMs) {
        this.controller = controller;
        this.ai = ai;
        this.timer = new Timer(tickMs, e -> updateOnce());
    }

    public OpponentManager(GameController controller, OpponentAI ai) {
        this(controller, ai, 100);
    }

    /** Start periodic updating */
    public void start() { timer.start(); }

    /** Stop periodic updating */
    public void stop() { timer.stop(); }

    /** Perform a single update (extracted for testing) */
    public void updateOnce() {
        int px = controller.getTargetX(0);
        int py = controller.getTargetY(0);
        OpponentAI.Target t = ai.nextTarget(px, py);
        controller.setTarget(1, t.x(), t.y());
    }
}