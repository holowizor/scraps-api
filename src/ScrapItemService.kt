package com.devbuild.api.scraps

import com.devbuild.commons.db.Database
import org.dizitart.kno2.filters.eq
import org.dizitart.kno2.getRepository
import org.dizitart.no2.NitriteId
import org.dizitart.no2.objects.Id
import java.io.Serializable

data class ScrapItem(
    @Id val id: Long, val scrapId: Long, val authorId: Long,
    val name: String,
    val content: String
) : Serializable {
    fun toDto(): ScrapItemDto = ScrapItemDto(id, scrapId, name, content)
    fun toDtoNoContent(): ScrapItemDto = ScrapItemDto(id, scrapId, name, "")
}

interface ScrapItemService {
    fun findById(id: Long): ScrapItem?
    fun findByScrapId(scrapId: Long): Collection<ScrapItem>
    fun createScrapItem(name: String, content: String, scrapId: Long, authorId: Long): ScrapItem
    fun updateScrapItem(id: Long, name: String, content: String)
    fun deleteScrapItem(id: Long)
}

class ScrapItemServiceImpl : ScrapItemService {
    override fun findById(id: Long): ScrapItem? =
        Database.db.getRepository<ScrapItem>().find(ScrapItem::id eq id).firstOrNull()

    override fun findByScrapId(scrapId: Long): Collection<ScrapItem> =
        Database.db.getRepository<ScrapItem>().find(ScrapItem::scrapId eq scrapId).toList()

    override fun createScrapItem(name: String, content: String, scrapId: Long, authorId: Long): ScrapItem {
        val newContent = ScrapItem(NitriteId.newId().idValue, scrapId, authorId, name, content)
        Database.db.getRepository<ScrapItem>().insert(newContent)
        return newContent
    }

    override fun updateScrapItem(id: Long, newName: String, newContent: String) {
        val content = findById(id)
        if (content != null) {
            val update = content.copy(name = newName, content = newContent)
            Database.db.getRepository<ScrapItem>().update(ScrapItem::id eq id, update)
        }
    }

    override fun deleteScrapItem(id: Long) {
        Database.db.getRepository<ScrapItem>().remove(ScrapItem::id eq id)
    }
}
