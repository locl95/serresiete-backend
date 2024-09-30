package com.kos.plugins

import com.kos.characters.CharacterCreateRequest
import com.kos.characters.LolCharacterRequest
import com.kos.characters.WowCharacterRequest
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic


fun Application.configureSerialization() {
    val sm = SerializersModule {
        polymorphic(CharacterCreateRequest::class) {
            subclass(LolCharacterRequest::class, LolCharacterRequest.serializer())
            subclass(WowCharacterRequest::class, WowCharacterRequest.serializer())
        }
    }

    install(ContentNegotiation) {
        json(
            json = Json {
                serializersModule = sm
                classDiscriminator = "type"
            }
        )
    }
}