package com.devbuild.api.scraps

import com.devbuild.commons.db.Database
import org.dizitart.kno2.filters.eq
import org.dizitart.kno2.getRepository
import org.dizitart.no2.NitriteId
import org.dizitart.no2.objects.Id
import java.io.Serializable

data class Scrap(@Id val id: Long, val name: String, val authorId: Long, val content: String = "") : Serializable {
    fun toDto(): ScrapDto = ScrapDto(id, name, content)
}

interface ScrapService {
    fun findAll(): Collection<Scrap>
    fun findByAuthorId(authorId: Long): Collection<Scrap>
    fun findById(id: Long): Scrap?
    fun createScrap(name: String, authorId: Long): Scrap
    fun updateScrap(id: Long, name: String, content: String)
    fun deleteScrap(id: Long)
}

class ScrapServiceImpl : ScrapService {
    override fun findAll(): Collection<Scrap> =
        Database.db.getRepository<Scrap>().find().toList()

    override fun findByAuthorId(authorId: Long): Collection<Scrap> =
        Database.db.getRepository<Scrap>().find(Scrap::authorId eq authorId).toList()

    override fun findById(id: Long): Scrap? =
        Database.db.getRepository<Scrap>().find(Scrap::id eq id).firstOrNull()

    override fun createScrap(name: String, authorId: Long): Scrap {
        val newScrap = Scrap(NitriteId.newId().idValue, name, authorId)
        Database.db.getRepository<Scrap>().insert(newScrap)
        return newScrap
    }

    override fun updateScrap(id: Long, name: String, content: String) {
        val category = findById(id)
        if (category != null) {
            val update = category.copy(name = name, content = content)
            Database.db.getRepository<Scrap>().update(Scrap::id eq id, update)
        }
    }

    override fun deleteScrap(id: Long) {
        Database.db.getRepository<Scrap>().remove(Scrap::id eq id)
    }
}
