package com.example.iampaw.components.report

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportDraftStore @Inject constructor() {

    private var draft: ReportDraft? = null

    fun save(draft: ReportDraft) {
        this.draft = draft
    }

    fun peek(): ReportDraft? = draft

    fun consume(): ReportDraft? = draft.also { draft = null }
}
