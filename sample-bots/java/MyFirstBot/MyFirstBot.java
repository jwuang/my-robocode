import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;
import dev.robocode.tankroyale.botapi.graphics.Color;

import java.awt.geom.Point2D;
import java.awt.geom.Line2D;
import java.util.HashMap;
import java.util.Map;

// ------------------------------------------------------------------
// MyFirstBot
// ------------------------------------------------------------------
// A competitive bot for Robocode Tank Royale
//
// Implements strategy inspired by Mushroom bot
// ------------------------------------------------------------------
public class MyFirstBot extends Bot {
    
    // Movement variables
    private Point2D.Double myLocation;
    private Point2D.Double enemyLocation;
    
    // Wall tracking
    public Map<Integer, Wall> detectedWalls = new HashMap<>();
    
    // The main method starts our bot
    public static void main(String[] args) {
        new MyFirstBot().start();
    }

    // Constructor, which loads the bot config file
    MyFirstBot() {
        super(BotInfo.fromFile("MyFirstBot.json"));
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        myLocation = new Point2D.Double(getX(), getY());
        enemyLocation = new Point2D.Double(0, 0);
        
        // Set colors
        setBodyColor(Color.fromRgb(0x00, 0xC8, 0x00));   // lime
        setTurretColor(Color.fromRgb(0x00, 0x96, 0x32)); // green
        setRadarColor(Color.fromRgb(0x00, 0x64, 0x64));  // dark cyan
        setBulletColor(Color.fromRgb(0xFF, 0xFF, 0x64)); // yellow
        setScanColor(Color.fromRgb(0xFF, 0xC8, 0xC8));   // light red
        
        // Enable gun and radar adjustment
        setAdjustGunForBodyTurn(true);
        setAdjustRadarForGunTurn(true);
        
        int t = 0;
        Point2D.Double preLocation = new Point2D.Double(getX(), getY());
        Point2D.Double curLocation = new Point2D.Double(getX(), getY());
        
        // Main loop
        do {
            // Rotate radar
            turnRadarRight(30);
            
            t++;
            if (t == 30) {
                // Check if we're stuck
                if (curLocation.getX() == preLocation.getX() && 
                    curLocation.getY() == preLocation.getY() && 
                    getEnergy() >= 30) {
                    forward(10);
                    back(10);
                }
                
                preLocation.setLocation(curLocation.getX(), curLocation.getY());
                curLocation.setLocation(getX(), getY());
                t = 0;
            }
            
            // Move
            doMove();
            
        } while (isRunning());
    }
    
    // Movement method
    private void doMove() {
        myLocation.setLocation(getX(), getY());
        
        // Calculate forces
        double forceX = 0;
        double forceY = 0;
        
        // Center attraction force
        double centerX = getArenaWidth() / 2;
        double centerY = getArenaHeight() / 2;
        forceX += (centerX - myLocation.x) / 2000;
        forceY += (centerY - myLocation.y) / 2000;
        
        // Wall avoidance
        avoidWalls(forceX, forceY);
        
        // Enemy positioning
        if (enemyLocation != null && 
            enemyLocation.x != 0 && enemyLocation.y != 0) {
            double distance = distanceTo(enemyLocation.x, enemyLocation.y);
            
            if (distance < 150) {
                // Move away from close enemies
                double dx = myLocation.x - enemyLocation.x;
                double dy = myLocation.y - enemyLocation.y;
                double length = Math.sqrt(dx * dx + dy * dy);
                
                if (length > 0) {
                    forceX += (dx / length) * (150 - distance) / 20;
                    forceY += (dy / length) * (150 - distance) / 20;
                }
            } else {
                // Move toward distant enemies
                double dx = enemyLocation.x - myLocation.x;
                double dy = enemyLocation.y - myLocation.y;
                double length = Math.sqrt(dx * dx + dy * dy);
                
                if (length > 0) {
                    forceX += (dx / length) * Math.min(distance, 300) / 1000;
                    forceY += (dy / length) * Math.min(distance, 300) / 1000;
                }
            }
        }
        
        // Convert to movement
        double angle = Math.atan2(forceY, forceX);
        double distance = Math.sqrt(forceX * forceX + forceY * forceY) * 100;
        
        if (distance < 20) {
            distance = 20;
        }
        
        // Turn and move
        setTurnRight(normalizeRelativeAngle(Math.toDegrees(angle) - getDirection()));
        setForward(distance);
    }
    
    // Wall avoidance
    private void avoidWalls(double forceX, double forceY) {
        // Arena wall avoidance
        if (myLocation.x < 50) {
            forceX += (50 - myLocation.x) / 10;
        } else if (myLocation.x > getArenaWidth() - 50) {
            forceX -= (myLocation.x - (getArenaWidth() - 50)) / 10;
        }
        
        if (myLocation.y < 50) {
            forceY += (50 - myLocation.y) / 10;
        } else if (myLocation.y > getArenaHeight() - 50) {
            forceY -= (myLocation.y - (getArenaHeight() - 50)) / 10;
        }
        
        // Custom wall avoidance
        Wall nearestWall = getNearestWall();
        if (nearestWall != null) {
            double distanceToWall = getDistanceToWall(nearestWall);
            if (distanceToWall < 200) {
                double wallDx = Math.cos(nearestWall.rotation + Math.PI/2);
                double wallDy = Math.sin(nearestWall.rotation + Math.PI/2);
                
                forceX += wallDx * (200 - distanceToWall) / 20;
                forceY += wallDy * (200 - distanceToWall) / 20;
            }
        }
    }
    
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        // Update enemy location
        enemyLocation.setLocation(e.getX(), e.getY());
        
        // Calculate bearing to enemy
        double enemyBearing = bearingTo(e.getX(), e.getY());
        
        // Turn gun toward enemy
        double gunTurn = normalizeRelativeAngle(enemyBearing - getGunDirection());
        setTurnGunRight(gunTurn);
        
        // Fire when gun is aimed reasonably close to enemy
        if (Math.abs(gunTurn) < 10) {
            fire(1);
        }
        
        rescan();
    }
    
    @Override
    public void onHitWall(HitWallEvent e) {
        setBack(100);
        setTurnRight(90);
        rescan();
    }
    
    @Override
    public void onHitBot(HitBotEvent e) {
        setBack(100);
        setTurnRight(45);
        rescan();
    }
    
    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        setTurnRight(45 + Math.random() * 90);
        setForward(100);
        rescan();
    }
    
    // Wall detection and avoidance
    public void onScannedWall(ScannedWallEvent e) {
        int wallId = e.getScannedBotId();
        Wall w = null;
        if (!detectedWalls.containsKey(wallId)) {
            w = new Wall(e);
            detectedWalls.put(wallId, w);
        } else {
            w = detectedWalls.get(wallId);
        }
        rescan();
    }
    
    // Get nearest wall
    private Wall getNearestWall() {
        Wall nearestWall = null;
        double minDistance = Double.MAX_VALUE;
        
        for (Wall w : detectedWalls.values()) {
            double distance = getDistanceToWall(w);
            if (distance < minDistance) {
                minDistance = distance;
                nearestWall = w;
            }
        }
        
        return nearestWall;
    }
    
    // Calculate distance to wall
    private double getDistanceToWall(Wall w) {
        // Using formula for distance from point to line
        double A = w.line.y2 - w.line.y1;
        double B = w.line.x1 - w.line.x2;
        double C = w.line.x2 * w.line.y1 - w.line.x1 * w.line.y2;
        
        double numerator = Math.abs(A * getX() + B * getY() + C);
        double denominator = Math.sqrt(A * A + B * B);
        return numerator / denominator;
    }
    
    // Wall class to represent scanned walls
    class Wall {
        int id;
        Line2D.Double line;
        Point2D.Double center;
        double rotation;
        
        public Wall(ScannedWallEvent e) {
            id = e.getScannedBotId();
            rotation = e.getRotation();
            center = new Point2D.Double(e.getX(), e.getY());
            
            // For a wall, we only consider its length (height in the event)
            double centerX = e.getX(), centerY = e.getY(), height = e.getHeight();
            double x1 = centerX + Math.cos(rotation) * height / 2;
            double y1 = centerY + Math.sin(rotation) * height / 2;
            double x2 = centerX - Math.cos(rotation) * height / 2;
            double y2 = centerY - Math.sin(rotation) * height / 2;
            line = new Line2D.Double(x1, y1, x2, y2);
        }
    }
}