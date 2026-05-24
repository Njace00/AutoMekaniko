package com.example.automekaniko

// DTC Guide Registry
// ------------------
// To add a new DTC guide:
//   1. Create a new file e.g. P0420Guide.kt
//   2. Define  val P0420Guide = DtcGuide(...)  in that file
//   3. Add P0420Guide to the list below — that's it.
// DtcActivity reads this list automatically, no other changes needed.

val dtcGuides: List<DtcGuide> = listOf(

    P0301Guide,
    P2118Guide,

    // P0420Guide,   // uncomment when you create P0420Guide.kt
    // P0171Guide,
    // P0300Guide,

)