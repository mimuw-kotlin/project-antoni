enum class TimePeriod(val limit: Int) {
    LAST_24_MONTHS(24),
    LAST_60_MONTHS(60),
    LAST_120_MONTHS(120),
    ALL_DATA(-1);
}