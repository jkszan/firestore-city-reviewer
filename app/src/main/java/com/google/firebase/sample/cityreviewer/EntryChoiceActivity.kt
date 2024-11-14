package com.google.firebase.sample.cityreviewer

import android.content.Intent
import com.firebase.example.internal.BaseEntryChoiceActivity
import com.firebase.example.internal.Choice

class EntryChoiceActivity : BaseEntryChoiceActivity() {

    override fun getChoices(): List<Choice> {
        return listOf(
            Choice(
                "Java",
                "Run the Firestore quickstart written in Java.",
                Intent(this, com.google.firebase.sample.cityreviewer.java.MainActivity::class.java),
            )
        )
    }
}
