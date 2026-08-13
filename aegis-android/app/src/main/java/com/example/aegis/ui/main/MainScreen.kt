package com.example.aegis.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var sosTriggered by remember { mutableStateOf(false) }
    var sosMessage by remember { mutableStateOf("SOS READY (HOLD 2s)") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP HEADER
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AEGIS SAFEPASS",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF06B6D4)
                )
                Text(
                    text = "Autonomous Emergency & Identity Safeguard",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
            }
            Surface(
                color = Color(0xFF10B981).copy(alpha = 0.2f),
                shape = CircleShape,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
            ) {
                Text(
                    text = "🟢 Mesh Active",
                    fontSize = 11.sp,
                    color = Color(0xFF10B981),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // PILLAR 1: DIGITAL TOURIST ID CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🛡️ DIGITAL TOURIST ID",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    Surface(
                        color = Color(0xFF8B5CF6).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "Sepolia On-Chain Proof",
                            fontSize = 10.sp,
                            color = Color(0xFFA78BFA),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Divider(color = Color(0xFF334155))

                Text(
                    text = "ID: TST-8F29X4",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Text(
                    text = "Hash: 0xa7f8e32904b1c5a92d831...",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                Text(
                    text = "Route: Shillong ➔ Cherrapunji ➔ Dawki",
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )

                Text(
                    text = "Validity: 12 Aug 2026 – 20 Aug 2026 (Active)",
                    fontSize = 11.sp,
                    color = Color(0xFF34D399)
                )

                // MOCK QR CODE CONTAINER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "[ ⬛⬛⬜⬛⬜⬛⬛ ]",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF06B6D4)
                        )
                        Text(
                            text = "[ ⬛⬜⬛⬛⬛⬜⬛ ]",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF06B6D4)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap to view full verification QR",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // PILLAR 2: ON-DEVICE GEOFENCE & GNSS STATUS
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "📍 ON-DEVICE GEOSPATIAL SAFETY",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF59E0B)
                )

                Surface(
                    color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "🟡 CAUTION ZONE",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFF59E0B)
                            )
                            Text(
                                text = "Cherrapunji Ridge • Heavy Rainfall Alert",
                                fontSize = 11.sp,
                                color = Color(0xFFFDE68A)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "GNSS Fix: 25.141° N, 91.261° E", fontSize = 11.sp, color = Color(0xFF94A3B8))
                    Text(text = "Satellites: 9 Lock", fontSize = 11.sp, color = Color(0xFF34D399))
                }

                Text(
                    text = "✓ Offline Raycasting Geofence Engine Active (0 Bytes Mobile Data Used)",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        // PILLAR 3: RESILIENT EMERGENCY SOS BUTTON
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (sosTriggered) Color(0xFF7F1D1D) else Color(0xFF991B1B)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🚨 EMERGENCY SOS PANIC TRIGGER",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Button(
                    onClick = {
                        sosTriggered = true
                        sosMessage = "🚨 SOS DISPATCHED OVER WEBSOCKET & SMS FALLBACK"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (sosTriggered) "SOS ACTIVE - HELP EN ROUTE" else "PRESS TO DISPATCH SOS",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Text(
                    text = sosMessage,
                    fontSize = 11.sp,
                    color = if (sosTriggered) Color(0xFFFCA5A5) else Color(0xFFFECACA),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Dual Dispatch: WebSockets + Compact SMS Payload (Zero-Cost)",
                    fontSize = 10.sp,
                    color = Color(0xFFF87171)
                )
            }
        }

        // PILLAR 4: P2P OFFLINE MESH RELAY STATUS
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "📡 P2P BLE / WI-FI DIRECT MESH RELAY",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFA78BFA)
                )
                Text(
                    text = "Connected Peers: 2 Tourist Devices in 15m radius",
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1)
                )
                Text(
                    text = "Dead-Zone Relay: Active (Emergency messages hop across peers until reaching cell coverage)",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
