package com.devbuild.api.scraps

import com.devbuild.commons.db.Database
import org.dizitart.kno2.filters.eq
import org.dizitart.kno2.getRepository
import org.dizitart.no2.NitriteId
import org.dizitart.no2.objects.Id
import java.io.Serializable

data class Scrap(@Id val id: Long, val name: String, val authorId: Long) : Serializable {
    fun toDto(): ScrapDto = ScrapDto(id, name)
}

interface ScrapService {
    fun findAll(): Collection<Scrap>
    fun findByAuthorId(authorId: Long): Collection<Scrap>
    fun findById(id: Long): Scrap?
    fun createCategory(name: String, authorId: Long): Scrap
    fun updateCategory(id: Long, name: String)
    fun deleteCategory(id: Long)
}

class ScrapServiceImpl : ScrapService {
    override fun findAll(): Collection<Scrap> =
        Database.db.getRepository<Scrap>().find().toList()

    override fun findByAuthorId(authorId: Long): Collection<Scrap> =
        Database.db.getRepository<Scrap>().find(Scrap::authorId eq authorId).toList()

    override fun findById(id: Long): Scrap? =
        Database.db.getRepository<Scrap>().find(Scrap::id eq id).firstOrNull()

    override fun createCategory(name: String, authorId: Long): Scrap {
        val newCategory = Scrap(NitriteId.newId().idValue, name, authorId)
        Database.db.getRepository<Scrap>().insert(newCategory)
        return newCategory
    }

    override fun updateCategory(id: Long, name: String) {
        val category = findById(id)
        if (category != null) {
            val update = category.copy(name = name)
            Database.db.getRepository<Scrap>().update(Scrap::id eq id, update)
        }
    }

    override fun deleteCategory(id: Long) {
        Database.db.getRepository<Scrap>().remove(Scrap::id eq id)
        Database.db.getRepository<ScrapItem>().remove(ScrapItem::scrapId eq id)
    }
}
