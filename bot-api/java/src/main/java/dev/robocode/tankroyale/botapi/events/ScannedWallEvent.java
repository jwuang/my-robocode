package dev.robocode.tankroyale.botapi.events;

/**
 * Event occurring when a bot has scanned a wall.
 */
@SuppressWarnings("unused")
public final class ScannedWallEvent extends BotEvent{

    // id of the bot did the scanning.
    private final int scannedByBotId;

    // id of the wall that was scanned.
    private final int scannedWallId;

    // X coordinate of the scanned wall.
    private final double x;

    // Y coordinate of the scanned wall.
    private final double y;

    // Width of the scanned wall.
    private final double width;

    // Height of the scanned wall.
    private final double height;

    // Rotation in degrees of the scanned bot.
    private final double rotation;

    /**
     * Initializes a new instance of the ScannedWallEvent class.
     *
     * @param turnNumber        is the turn number when the bot was scanned.
     * @param scannedByBotId    is the id of the bot did the scanning.
     * @param scannedWallId     is the id of the wall that was scanned.
     * @param x                 is the X coordinate of the center of the scanned wall.
     * @param y                 is the Y coordinate of the center of the scanned wall.
     * @param width             is the width of the scanned wall.
     * @param height            is the height of the scanned wall.
     * @param rotation          is the rotation of the scanned wall.
     */
    public ScannedWallEvent(
            int turnNumber,
            int scannedByBotId,
            int scannedWallId,
            double x,
            double y,
            double width,
            double height,
            double rotation) {
        super(turnNumber);
        this.scannedByBotId = scannedByBotId;
        this.scannedWallId = scannedWallId;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.rotation = rotation;
    }
    /**
     * Returns the id of the bot did the scanning.
     *
     * @return The id of the bot did the scanning.
     */
    public int getScannedByBotId() {
        return scannedByBotId;
    }

    /**
     * Returns the id of the wall that was scanned.
     *
     * @return The id of the wall that was scanned.
     */
    public int getScannedWallId() {
        return scannedWallId;
    }

    /**
     * Returns the X coordinate of the scanned wall.
     *
     * @return The X coordinate of the scanned wall.
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the Y coordinate of the scanned wall.
     *
     * @return The Y coordinate of the scanned wall.
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the width of the scanned wall.
     *
     * @return The width of the scanned wall.
     */
    public double getWidth() {
        return width;
    }

    /**
     * Returns the height of the scanned wall.
     *
     * @return The height of the scanned wall.
     */
    public double getHeight() {
        return height;
    }

    /**
     * Returns the rotation of the scanned wall.
     *
     * @return The rotation of the scanned wall.
     */
    public double getRotation(){return rotation;}
}