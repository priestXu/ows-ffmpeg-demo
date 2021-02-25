package com.goodsogood.ows.helper.bean

data class ImageCheckResult(
    val result: Result
)

data class Result(
    val category_suggestions: CategorySuggestions,
    val detail: Detail,
    val suggestion: String
)

data class CategorySuggestions(
    val ad: String,
    val politics: String,
    val porn: String,
    val terrorism: String
)

data class Detail(
    val ad: List<Ad>,
    val politics: List<Any>,
    val porn: List<Porn>,
    val terrorism: List<Terrorism>
)

data class Ad(
    val confidence: Double,
    val label: String
)

data class Porn(
    val confidence: Double,
    val label: String
)

data class Terrorism(
    val confidence: Double,
    val label: String
)