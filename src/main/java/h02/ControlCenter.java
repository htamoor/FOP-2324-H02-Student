package h02;

import fopbot.Direction;
import fopbot.Robot;
import fopbot.World;

import static org.tudalgo.algoutils.student.Student.crash;

/**
 * A class that controls the {@linkplain Robot robots} and their actions.
 */
public class ControlCenter {

    /**
     * Creates a new line of {@linkplain ScanRobot ScanRobots}.
     *
     * @return An array containing the newly initialised robots
     */
    public ScanRobot[] initScanRobots() {
        ScanRobot[] scanRobots = new ScanRobot[World.getWidth() - 1];
        for (int i = 0; i < scanRobots.length; i++) {
            scanRobots[i] = new ScanRobot(i + 1, 0, Direction.UP, 0);
        }
        return scanRobots;
    }

    /**
     * Creates a new line of {@linkplain CleanRobot CleanRobots}.
     *
     * @return An array containing the newly initialised robots
     */
    public CleanRobot[] initCleaningRobots() {
        CleanRobot[] cleanRobots = new CleanRobot[World.getHeight() - 1];
        for (int i = 0; i < cleanRobots.length; i++) {
            cleanRobots[i] = new CleanRobot(0, i + 1, Direction.RIGHT, 0);
        }

        return cleanRobots;
    }

    /**
     * Inverts the given array by swapping the first and last entry, continuing with the second and second last entry and so on until the entire array has been inverted.
     *
     * @param robots The array to invert
     */
    public void reverseRobots(Robot[] robots) {
        for (int i = 0; i < robots.length / 2; i++) {
            Robot tmp = robots[i];
            robots[i] = robots[robots.length - i - 1];
            robots[robots.length - i - 1] = tmp;

        }
    }

    /**
     * Rotates the {@linkplain Robot robots} in the given array in ascending order and calls {@link #checkForDamage} on every {@link Robot} after its rotation.
     *
     * @param robots The array of {@linkplain Robot robots} to rotate
     */
    public void rotateRobots(Robot[] robots) {
        for (Robot robot : robots) {
            Direction toDirection;
            switch (robot.getDirection()) {
                case UP -> toDirection = Direction.DOWN;
                case DOWN -> toDirection = Direction.UP;
                case LEFT -> toDirection = Direction.RIGHT;
                case RIGHT -> toDirection = Direction.LEFT;
                default -> throw new IllegalArgumentException("Ups...Something went wrong!");
            }
            while (robot.getDirection() != toDirection) {
                robot.turnLeft();
            }
            checkForDamage(robot);
        }
    }

    /**
     * Simulates inspecting a {@link Robot} for wear, turning it off if it should no longer serve. Currently implemented as a coin flip.
     *
     * @param robot The {@link Robot} to inspect
     */
    public void checkForDamage(Robot robot) {
        final double p = 0.5;
        if (Math.random() > p) {
            robot.turnOff();
        }
    }

    /**
     * Replaces the {@linkplain Robot robots} that are turned off in the provided array with new ones. <br>
     * The method expects either an array of {@linkplain ScanRobot ScanRobots} or {@linkplain CleanRobot CleanRobots} and uses the correct class when replacing the robots.
     *
     * @param robots An array possibly containing {@linkplain Robot robots} that are turned off and need to be replaced
     */
    public void replaceBrokenRobots(Robot[] robots) {
        for (int i = 0; i < robots.length; i++) {
            if (robots[i].isTurnedOff()) {
                if (isScanRobotArray(robots)) {
                    robots[i] = new ScanRobot(robots[i].getX(), robots[i].getY(), robots[i].getDirection(), robots[i].getNumberOfCoins());
                } else {
                    robots[i] = new CleanRobot(robots[i].getX(), robots[i].getY(), robots[i].getDirection(), robots[i].getNumberOfCoins());
                }
            }
        }
    }

    /**
     * Tests whether the given array is an array of {@linkplain ScanRobot ScanRobots} or not.
     *
     * @param robots The array to test
     * @return Whether the given array is an array of {@linkplain ScanRobot ScanRobots}
     */
    public boolean isScanRobotArray(Robot[] robots) {
        return robots instanceof ScanRobot[];
    }

    /**
     * Calls {@link #reverseRobots}, {@link #rotateRobots} and {@link #replaceBrokenRobots} in that order, with the given array as the argument
     *
     * @param robots The array to perform the aforementioned actions on
     */
    public void spinRobots(Robot[] robots) {
        reverseRobots(robots);
        rotateRobots(robots);
        replaceBrokenRobots(robots);
    }

    /**
     * Moves the robots to the end of the world, in ascending order and one at a time.
     *
     * @param robots The robots to move
     */
    public void returnRobots(Robot[] robots) {
        for (Robot robot : robots) {
            while (robot.isFrontClear()) {
                robot.move();
            }
        }
    }

    /**
     * Scans the world using the provided {@linkplain ScanRobot ScanRobots} and returns an array containing the scan results.
     *
     * @param scanRobots The robots to scan the world with
     * @return An array detailing which world fields contain at least one coin
     */
    public boolean[][] scanWorld(ScanRobot[] scanRobots) {
        boolean[][] coinPositions = new boolean[World.getHeight()][World.getWidth()];
        boolean reachedtheEnd = false;

        while (!reachedtheEnd) {
            for (ScanRobot bot : scanRobots) {
                if (!bot.isFrontClear()) {
                    reachedtheEnd = true;
                    break;
                }
                bot.move();
                if (bot.isOnACoin()) {
                    coinPositions[bot.getY()][bot.getX()] = true;
                }
            }
        }
        // if the while loop has terminated, the end of the world has been reached, so spin the robots and return
        spinRobots(scanRobots);
        returnRobots(scanRobots);
        spinRobots(scanRobots);
        return coinPositions;
    }

    /**
     * Performs one iteration of collecting coins, using the provided arrays to clean and determine where to clean.
     *
     * @param coinPositions An array with all the coin positions to be collected
     * @param cleanRobots   An array containing the {@linkplain CleanRobot CleanRobots} to collect the coins with.
     */
    public void moveCleanRobots(CleanRobot[] cleanRobots, boolean[][] coinPositions) {
        boolean reachedTheEnd = false;
        while (!reachedTheEnd) {
            for (CleanRobot bot : cleanRobots) {
                if (!bot.isFrontClear()) {
                    reachedTheEnd = true;
                    break;
                }
                bot.move();
                if (coinPositions[bot.getY()][bot.getX()]) {
                    bot.pickCoin();
                }
            }
        }
        spinRobots(cleanRobots);
        returnRobots(cleanRobots);
        spinRobots(cleanRobots);
    }

    /**
     * Collects all the coins in the world using all the previously implemented helper methods.
     */
    public void cleanWorld() {
        ScanRobot[] scanRobots = initScanRobots();
        CleanRobot[] cleanRobots = initCleaningRobots();
        boolean coinsGathered = false;
        while (!coinsGathered) {
            boolean[][] coinsInWorld = scanWorld(scanRobots);
            if (allCoinsGathered(coinsInWorld)) {
                break;
            }
            moveCleanRobots(cleanRobots, coinsInWorld);
            coinsGathered = allCoinsGathered(coinsInWorld);
        }
        System.out.println("Finished cleaning the world!");
    }

    /**
     * Returns whether there are no coins left in the world.
     *
     * @param coins The array to search for coins
     * @return Whether the provided array contains at least one entry that is not false
     */
    public boolean allCoinsGathered(boolean[][] coins) {
        for (boolean[] coinRow : coins) {
            for (boolean b : coinRow) {
                if (b) {
                    return false;
                }
            }
        }
        return true;
    }
}
