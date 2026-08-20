package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.engine.FinancialEngine
import com.example.domain.model.GoalItem
import com.example.domain.model.ReceiptScanResult
import com.example.ui.theme.BorderGlass
import com.example.ui.theme.BorderGlassLight
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.WarningAmber

@Composable
fun dialogTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryBlue,
    unfocusedBorderColor = BorderGlass,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedLabelColor = PrimaryBlue,
    unfocusedLabelColor = TextSecondary,
    cursorColor = PrimaryBlue
)

@Composable
fun AddTransactionDialog(
    isIncomeMode: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, amount: Double, method: String, notes: String, isFamily: Boolean) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(if (isIncomeMode) "Salary" else "Food") }
    var method by remember { mutableStateOf("UPI") }
    var notes by remember { mutableStateOf("") }
    var isFamily by remember { mutableStateOf(true) }

    val categories = if (isIncomeMode) {
        listOf("Salary", "Freelance", "Investment Return", "Gift", "Rental", "Other")
    } else {
        listOf("Food", "Rent", "Bills", "Travel", "Shopping", "Entertainment", "Healthcare", "Education", "Other")
    }

    val methods = listOf("UPI", "Credit Card", "Debit Card", "Net Banking", "Cash")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = if (isIncomeMode) "Record New Income" else "Record New Expense",
                fontWeight = FontWeight.Bold,
                color = if (isIncomeMode) SuccessGreen else DangerRed
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Description (e.g. Supermarket, Salary)", color = TextSecondary) },
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount (₹)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Chips
                Text("Category", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(4).forEach { cat ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (category == cat) PrimaryBlue.copy(alpha = 0.3f) else DarkSurfaceVariant)
                                .border(1.dp, if (category == cat) PrimaryBlue else BorderGlassLight, RoundedCornerShape(8.dp))
                                .clickable { category = cat }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(cat, fontSize = 11.sp, color = if (category == cat) TextPrimary else TextSecondary)
                        }
                    }
                }

                // Payment Method Chips
                Text("Payment Method", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    methods.take(3).forEach { m ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (method == m) CyanNeon.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                .border(1.dp, if (method == m) CyanNeon else BorderGlassLight, RoundedCornerShape(8.dp))
                                .clickable { method = m }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(m, fontSize = 11.sp, color = if (method == m) TextPrimary else TextSecondary)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isFamily,
                        onCheckedChange = { isFamily = it },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                    )
                    Text("Share with Family Wallet", fontSize = 12.sp, color = TextSecondary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && amt > 0) {
                        onConfirm(title, category, amt, method, notes, isFamily)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isIncomeMode) SuccessGreen else PrimaryBlue)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, emoji: String, targetAmount: Double, targetDate: String, category: String, isFamily: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🎯") }
    var targetAmountStr by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("Dec 2026") }
    var category by remember { mutableStateOf("Savings") }
    var isFamily by remember { mutableStateOf(true) }

    val emojis = listOf("🎯", "🛡️", "✈️", "🚗", "🏡", "🎓", "💍", "💻")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Create Savings Goal", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Emoji Picker
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    emojis.forEach { e ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (emoji == e) PrimaryBlue.copy(alpha = 0.3f) else Color.Transparent)
                                .border(1.dp, if (emoji == e) PrimaryBlue else BorderGlass, RoundedCornerShape(8.dp))
                                .clickable { emoji = e }
                                .padding(6.dp)
                        ) {
                            Text(text = e, fontSize = 18.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name (e.g. Japan Trip, Emergency Buffer)", color = TextSecondary) },
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetAmountStr,
                    onValueChange = { targetAmountStr = it },
                    label = { Text("Target Amount (₹)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    label = { Text("Target Milestone Date (e.g. May 2027)", color = TextSecondary) },
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isFamily,
                        onCheckedChange = { isFamily = it },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                    )
                    Text("Shared Family Goal", fontSize = 12.sp, color = TextSecondary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = targetAmountStr.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amt > 0) {
                        onConfirm(name, emoji, amt, targetDate, category, isFamily)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Create Goal", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun TopUpGoalDialog(
    goal: GoalItem,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var amountStr by remember { mutableStateOf("5000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("${goal.emoji} Top-Up: ${goal.name}", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Current: ${FinancialEngine.formatINR(goal.currentAmount)} / Target: ${FinancialEngine.formatINR(goal.targetAmount)}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Deposit Amount (₹)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0) onConfirm(amt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Text("Deposit", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: String, limit: Double) -> Unit
) {
    var category by remember { mutableStateOf("Food") }
    var limitStr by remember { mutableStateOf("10000") }

    val categories = listOf("Food", "Rent & Housing", "Bills & Utilities", "Travel & Fuel", "Shopping", "Entertainment", "Healthcare", "Education")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Set Monthly Budget", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category Name", color = TextSecondary) },
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { limitStr = it },
                    label = { Text("Monthly Limit (₹)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lim = limitStr.toDoubleOrNull() ?: 0.0
                    if (category.isNotBlank() && lim > 0) {
                        onConfirm(category, lim)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Save Budget", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun AddBillDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, amount: Double, dueDate: String, category: String, isRecurring: Boolean, autoPay: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("28 Aug 2026") }
    var category by remember { mutableStateOf("Utilities") }
    var isRecurring by remember { mutableStateOf(true) }
    var autoPay by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text("Add Upcoming Bill", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Bill Name (e.g. Internet, Electricity)", color = TextSecondary) },
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Amount Due (₹)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (e.g. 28 Aug 2026)", color = TextSecondary) },
                    colors = dialogTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = autoPay,
                        onCheckedChange = { autoPay = it },
                        colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                    )
                    Text("Enable UPI Auto-Pay", fontSize = 12.sp, color = TextSecondary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountStr.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amt > 0) {
                        onConfirm(name, amt, dueDate, category, isRecurring, autoPay)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = WarningAmber)
            ) {
                Text("Add Bill", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}

@Composable
fun ReceiptPreviewDialog(
    scan: ReceiptScanResult,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🧾 Receipt OCR Scanned", fontWeight = FontWeight.Bold, color = TextPrimary)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Merchant: ${scan.merchantName}", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                Text("Total Amount: ${FinancialEngine.formatINR(scan.amount)}", fontWeight = FontWeight.ExtraBold, color = SuccessGreen, fontSize = 16.sp)
                Text("Category: ${scan.category} • Mode: ${scan.paymentMode}", color = TextSecondary, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(4.dp))
                Text("Extracted Line Items:", fontWeight = FontWeight.Bold, color = TextMuted, fontSize = 11.sp)
                scan.detectedItems.forEach { item ->
                    Text("• $item", fontSize = 11.sp, color = TextSecondary)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Add to Expenses", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Discard", color = TextMuted)
            }
        }
    )
}
