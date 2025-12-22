import yfiles.lang.IComparable

data class Person(
    val firstName: String,
    val lastName: String,
)

data class YDate(
    val milliseconds: Int,
) : IComparable<YDate> {
    override fun compareTo(other: YDate): Int {
        return milliseconds.compareTo(other.milliseconds)
    }
}
