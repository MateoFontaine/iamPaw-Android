package com.example.iampaw.data

import com.example.iampaw.components.detail.DetailState
import com.example.iampaw.components.feed.DogPost
import com.example.iampaw.components.match.MatchedDog

class PawMockDataSource : IPawDataSource {

    override fun getFeedDogs(): List<DogPost> {
        return listOf(
            DogPost("1", "Rocco", "Golden Retriever", "Palermo, CABA", "Hace 2h", "https://images.unsplash.com/photo-1552053831-71594a27632d?q=80&w=1000", "Perdido"),
            DogPost("2", "Luna", "Border Collie", "Córdoba, Arg", "Hace 5h", "https://images.unsplash.com/photo-1507146426996-ef05306b995a?q=80&w=1000", "Encontrado"),
            DogPost("3", "Milo", "Pug", "Rosario, SF", "Ayer", "https://images.unsplash.com/photo-1517849845537-4d257902454a?q=80&w=1000", "Perdido")
        )
    }

    override fun getDogDetail(id: String): DetailState {
        return DetailState(
            name = "Bobby",
            breed = "Golden Retriever",
            location = "Pinamar Centro",
            imageUrl = "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&q=80&w=600",
            description = "Se perdió cerca de la plaza central. Es súper manso pero está asustado. Tiene un collar de cuero marrón sin chapita.",
            aiAnalysis = "• Tamaño: Grande\n• Color: Dorado / Crema\n• Particularidades: Collar de cuero",
            isLost = true
        )
    }

    override fun getMatchedDogs(): List<MatchedDog> {
        return listOf(
            MatchedDog("Bobby", "Golden Retriever", "Pinamar Centro", "Perdido hace 2 días", 96, "https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&q=80&w=600"),
            MatchedDog("Sin nombre", "Mestizo / Labrador", "Cariló", "Visto merodeando hoy", 81, "https://images.unsplash.com/photo-1591768575198-88dac53fbd0a?auto=format&fit=crop&q=80&w=600")
        )
    }
}