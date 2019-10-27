import yfiles.view.IBoundsProvider
import yfiles.view.IVisibilityTestable

abstract class ComboClass : IVisibilityTestable, IBoundsProvider

abstract class SuperComboClass : ComboClass() {
    val puper = true
}