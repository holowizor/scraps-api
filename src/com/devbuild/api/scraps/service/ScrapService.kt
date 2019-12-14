package com.devbuild.api.scraps.service

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.transactions.transaction

class ScrapDTO {
    var id: Int = 0
    var authorId: Int = 0
    var name: String = ""
    var content: String = ""
}

object Scraps : IntIdTable() {
    val authorId: Column<Int> = integer("authorId")
    val name: Column<String> = varchar("name", 512)
    val content: Column<String> = text("content")
}

class Scrap(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Scrap>(
        Scraps
    )

    var authorId by Scraps.authorId
    var name by Scraps.name
    var content by Scraps.content

    fun toDTO(): ScrapDTO {
        val dto = ScrapDTO()
        dto.id = this.id.value
        dto.authorId = this.authorId
        dto.name = this.name
        dto.content = this.content
        return dto
    }
}

interface ScrapService {
    fun findByAuthorId(authorId: Int): Collection<Scrap>
    fun findById(id: Int): Scrap?
    fun createScrap(name: String, authorId: Int): Scrap
    fun updateScrap(id: Int, authorId: Int, name: String, content: String): Scrap?
    fun deleteScrap(id: Int, authorId: Int)
}

class ScrapServiceImpl : ScrapService {

    override fun findByAuthorId(authorId: Int): Collection<Scrap> =
        transaction { Scrap.find { Scraps.authorId eq authorId }.toList() }

    override fun findById(id: Int): Scrap? = transaction { Scrap.findById(id) }

    override fun createScrap(name: String, authorId: Int): Scrap = transaction {
        Scrap.new {
            this.name = name
            this.content = ""
            this.authorId = authorId
        }
    }

    override fun updateScrap(id: Int, authorId: Int, name: String, content: String): Scrap? {
        val scrap = findById(id)
        if (scrap != null) {
            if (scrap.authorId != authorId) return null
            transaction {
                scrap.name = name
                scrap.content = content
            }
        }
        return scrap
    }

    override fun deleteScrap(id: Int, authorId: Int) {
        val scrap = findById(id)
        if (scrap != null) {
            if (scrap.authorId != authorId) return
            transaction {
                scrap.delete()
            }
        }
    }
}
