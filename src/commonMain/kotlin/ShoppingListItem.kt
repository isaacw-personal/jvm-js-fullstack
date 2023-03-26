import kotlinx.serialization.Serializable

@Serializable
data class ShoppingListItem(val desc: String, val priority: Int) {
    val id: Int = desc.hashCode()
    val description : String = desc

    companion object {
        const val path = "/shoppingList"
    }
}