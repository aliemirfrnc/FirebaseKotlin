package com.example.firebasekotlin.model

class SohbetOdasi {

    var sohbetodasi_adi: String? = null
    var olusturan_id: String? = null
    var sohbetodasi_id: String? = null
    var sohbet_odasi_mesajlari:List<SohbetMesaj>? = null

    constructor() {}

    constructor(sohbetodasi_adi: String, olusturan_id: String, sohbetodasi_id: String, sohbet_odasi_mesajlari:List<SohbetMesaj>) {
        this.sohbetodasi_adi = sohbetodasi_adi
        this.olusturan_id = olusturan_id
        this.sohbetodasi_id = sohbetodasi_id
        this.sohbet_odasi_mesajlari=sohbet_odasi_mesajlari
    }
}