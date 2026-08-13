package com.example.aegis.ui.id

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aegis.domain.model.TouristIdentity
import com.example.aegis.domain.usecase.GetTouristIdentityUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class TouristIdUiState(
  val identity: TouristIdentity? = null,
  val onChainHash: String? = null,
  val issuanceNote: String =
    "Identity issuance & on-chain registration are not wired yet — this voucher is a preview.",
)

class TouristIdViewModel(
  observeIdentity: GetTouristIdentityUseCase,
) : ViewModel() {

  val uiState: StateFlow<TouristIdUiState> =
    observeIdentity()
      .map { identity ->
        TouristIdUiState(
          identity = identity,
          onChainHash = identity.onChainHash,
        )
      }
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TouristIdUiState())
}
