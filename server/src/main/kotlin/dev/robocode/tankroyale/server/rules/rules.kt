package dev.robocode.tankroyale.server.rules

/**
 * 子弹/碰撞/移动/扫描/生存消耗得分设定，进攻得分目前相对较高，生存得分较低
 */

/** Arena minimum size (width / height) */
const val ARENA_MIN_SIZE = 400

/** Arena maximum size (width / height) */
const val ARENA_MAX_SIZE = 5000

/** Minimum gun cooling rate */
const val MIN_GUN_COOLING_RATE = 0.1

/** Maximum gun cooling rate */
const val MAX_GUN_COOLING_RATE = 3.0

/** Initial bot energy level */
const val INITIAL_BOT_ENERGY = 300.0

/** Initial droid bot energy level */
const val INITIAL_DROID_ENERGY = 520.0

/** Initial gun heat */
const val INITIAL_GUN_HEAT = 3.0

/** Bot bounding circle diameter */
const val BOT_BOUNDING_CIRCLE_DIAMETER = 36

/** Bot bounding circle radius */
const val BOT_BOUNDING_CIRCLE_RADIUS = BOT_BOUNDING_CIRCLE_DIAMETER / 2.0

/** Radar radius */
const val RADAR_RADIUS = 1200.0

/** Maximum driving turn rate */
const val MAX_TURN_RATE = 10.0

/** Maximum gun turn rate */
const val MAX_GUN_TURN_RATE = 20.0

/** Maximum radar turn rate */
const val MAX_RADAR_TURN_RATE = 45.0

/** Maximum forward speed */
const val MAX_FORWARD_SPEED = 8.0

/** Maximum backward speed */
const val MAX_BACKWARD_SPEED = -8.0

/** Minimum firepower */
const val MIN_FIREPOWER = 0.1

/** Maximum firepower */
const val MAX_FIREPOWER = 3.0

/** Minimum bullet speed */
val MIN_BULLET_SPEED = calcBulletSpeed(MAX_FIREPOWER)

/** Maximum bullet speed */
val MAX_BULLET_SPEED = calcBulletSpeed(MIN_FIREPOWER)

/** Acceleration */
const val ACCELERATION = 1.0

/** Deceleration */
const val DECELERATION = -2.0

/** Ram damage */
const val RAM_DAMAGE = 3.0  // 0.6

/** Energy gain factor, when bullet hits */
const val BULLET_HIT_ENERGY_GAIN_FACTOR = 3

/** Score per survival */
const val SCORE_PER_SURVIVAL = 20.0

/** Bonus for last survival */
const val BONUS_PER_LAST_SURVIVOR = 20.0

/** Score per bullet damage */
const val SCORE_PER_BULLET_DAMAGE = 2.0    // 1.0

/** Bonus per bullet kill */
const val BONUS_PER_BULLET_KILL = 0.6

/** Score per ram damage */
const val SCORE_PER_RAM_DAMAGE = 4.0

/** Bonus per ram kill */
const val BONUS_PER_RAM_KILL = 0.6

/** Inactivity punishment damage per turn */
const val INACTIVITY_DAMAGE = 3.0    // 0.1,需要其他参数对比

/** Max. number of characters allowed for a team message */
const val MAX_TEAM_MESSAGE_SIZE = 4096

/** Max. number of team messages per turn */
const val MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN = 5

/** Movement penalty */
const val MOVEMENT_PENALTY = -0.05

/** Radar penalty */
const val RADAR_PENALTY = -0.02

/** Turn around penalty */
const val TURN_AROUND_PENALTY = -0.03