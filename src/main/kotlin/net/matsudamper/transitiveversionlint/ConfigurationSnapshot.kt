package net.matsudamper.transitiveversionlint

import java.io.Serializable

data class ConfigurationSnapshot(
    val name: String,
    val dependencies: List<ResolvedDependencySnapshot>,
) : Serializable

data class ResolvedDependencySnapshot(
    val selectedId: ModuleId,
    val fromId: DependsFrom,
    val requestedVersion: String,
) : Serializable

sealed interface DependsFrom : Serializable {
    data class Module(val name: String) : DependsFrom, Serializable
    data class Library(val id: ModuleId) : DependsFrom, Serializable
}

data class ModuleId(
    val group: String,
    val module: String,
    val version: String,
) : Serializable {
    override fun toString(): String {
        return "$group:$module:$version"
    }
}
