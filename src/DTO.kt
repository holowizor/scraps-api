package com.devbuild.api.scraps

import java.util.*

class ScrapDto(val id: Long?, val name: String)

class ScrapItemDto(val id: Long?, val scrapId: Long, val name: String, val content: String)

class ScrapWithItemsDto(val scrap: ScrapDto, val items: Collection<ScrapItemDto> = LinkedList())
