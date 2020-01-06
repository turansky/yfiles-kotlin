import yfiles.lang.IComparable
import yfiles.lang.YObject

data class Person(
    val firstName: String,
    val lastName: String
) : YObject

data class YDate(
    val milliseconds: Int
) : IComparable<YDate> {
    override fun compareTo(other: YDate): Int {
        return milliseconds.compareTo(other.milliseconds)
    }
}
