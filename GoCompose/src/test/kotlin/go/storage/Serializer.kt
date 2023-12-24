package go.storage

interface Seriealizer<Data> {
    fun serialize(data: Data): String
    fun deserialize(text: String): Data
}