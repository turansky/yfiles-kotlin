import yfiles.layout.LayoutOrientation

@JsExport
@ExperimentalJsExport
class EnumPolygon {
    fun enumTest() {
        val values = LayoutOrientation.values()
        val topToBottom = LayoutOrientation.valueOf("TOP_TO_BOTTOM")

        val name = LayoutOrientation.BOTTOM_TO_TOP.name
        val ordinal = LayoutOrientation.LEFT_TO_RIGHT.ordinal

        val s1 = LayoutOrientation.RIGHT_TO_LEFT.toString()

        val s2_1 = "${LayoutOrientation.TOP_TO_BOTTOM}"
        val s2_2 = "S2_2: ${LayoutOrientation.TOP_TO_BOTTOM}"

        val s3_1 = "" + LayoutOrientation.TOP_TO_BOTTOM
        val s3_2 = "S3_2: " + LayoutOrientation.TOP_TO_BOTTOM
    }
}
