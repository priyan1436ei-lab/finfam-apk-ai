package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.model.BillEntity
import com.example.data.local.model.BudgetEntity
import com.example.data.local.model.EmiEntity
import com.example.data.local.model.FamilyMemberEntity
import com.example.data.local.model.GoalEntity
import com.example.data.local.model.PaymentOrderEntity
import com.example.data.local.model.ScanHistoryEntity
import com.example.data.local.model.TransactionEntity
import com.example.data.local.model.UserProfileEntity
import com.example.domain.model.BillItem
import com.example.domain.model.BudgetItem
import com.example.domain.model.EmiItem
import com.example.domain.model.FamilyMemberItem
import com.example.domain.model.GoalItem
import com.example.domain.model.TransactionItem
import com.example.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FinGuardRepository(private val database: AppDatabase) {

    val userProfile: Flow<UserProfile> = database.userProfileDao().getUserProfile().map { entity ->
        if (entity != null) {
            UserProfile(
                id = entity.id,
                name = entity.name,
                email = entity.email,
                phone = entity.phone,
                initials = entity.name.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").ifBlank { "PS" },
                currencySymbol = entity.currencySymbol,
                totalBalance = entity.totalBalance,
                monthlyIncome = entity.monthlyIncome,
                monthlyExpenses = entity.monthlyExpenses,
                monthlySavings = entity.monthlySavings,
                emergencyFund = entity.emergencyFund,
                healthScore = entity.healthScore,
                previousHealthScore = entity.previousHealthScore,
                isPremium = entity.isPremium,
                premiumTier = entity.premiumTier,
                premiumValidUntil = entity.premiumValidUntil,
                familyId = entity.familyId,
                familyName = entity.familyName,
                isBiometricEnabled = entity.isBiometricEnabled,
                isNotificationsEnabled = entity.isNotificationsEnabled
            )
        } else {
            UserProfile()
        }
    }

    val transactions: Flow<List<TransactionItem>> = database.transactionDao().getAllTransactions().map { list ->
        list.map { entity ->
            TransactionItem(
                id = entity.id,
                title = entity.title,
                category = entity.category,
                amount = entity.amount,
                type = entity.type,
                isCredit = entity.isCredit,
                date = entity.date,
                timestamp = entity.timestamp,
                paymentMethod = entity.paymentMethod,
                notes = entity.notes,
                receiptUrl = entity.receiptUrl,
                isFamilyShared = entity.isFamilyShared,
                memberName = entity.memberName,
                iconName = entity.iconName,
                riskStatus = entity.riskStatus
            )
        }
    }

    val budgets: Flow<List<BudgetItem>> = database.budgetDao().getAllBudgets().map { list ->
        list.map { entity ->
            BudgetItem(
                id = entity.id,
                category = entity.category,
                monthlyLimit = entity.monthlyLimit,
                spent = entity.spent,
                month = entity.month,
                iconName = entity.iconName,
                alertThreshold80 = entity.alertThreshold80,
                alertThreshold90 = entity.alertThreshold90,
                alertThreshold100 = entity.alertThreshold100
            )
        }
    }

    val goals: Flow<List<GoalItem>> = database.goalDao().getAllGoals().map { list ->
        list.map { entity ->
            GoalItem(
                id = entity.id,
                name = entity.name,
                emoji = entity.emoji,
                targetAmount = entity.targetAmount,
                currentAmount = entity.currentAmount,
                targetDate = entity.targetDate,
                category = entity.category,
                isFamilyGoal = entity.isFamilyGoal
            )
        }
    }

    val bills: Flow<List<BillItem>> = database.billDao().getAllBills().map { list ->
        list.map { entity ->
            BillItem(
                id = entity.id,
                name = entity.name,
                amount = entity.amount,
                dueDate = entity.dueDate,
                dueTimestamp = entity.dueTimestamp,
                category = entity.category,
                isRecurring = entity.isRecurring,
                isPaid = entity.isPaid,
                reminderDays = entity.reminderDays,
                autoPayEnabled = entity.autoPayEnabled
            )
        }
    }

    val familyMembers: Flow<List<FamilyMemberItem>> = database.familyMemberDao().getAllMembers().map { list ->
        list.map { entity ->
            FamilyMemberItem(
                id = entity.id,
                name = entity.name,
                role = entity.role,
                email = entity.email,
                avatarColor = entity.avatarColor,
                monthlyContribution = entity.monthlyContribution,
                spentThisMonth = entity.spentThisMonth,
                salaryIncome = entity.salaryIncome,
                freelanceIncome = entity.freelanceIncome,
                businessIncome = entity.businessIncome,
                rentalIncome = entity.rentalIncome,
                otherIncome = entity.otherIncome,
                foodExpense = entity.foodExpense,
                transportExpense = entity.transportExpense,
                shoppingExpense = entity.shoppingExpense,
                educationExpense = entity.educationExpense,
                healthExpense = entity.healthExpense,
                entertainmentExpense = entity.entertainmentExpense,
                bankSavings = entity.bankSavings,
                emergencyFund = entity.emergencyFund,
                fixedDeposit = entity.fixedDeposit,
                mutualFund = entity.mutualFund,
                monthlyEmi = entity.monthlyEmi,
                equityInvestments = entity.equityInvestments,
                goldInvestments = entity.goldInvestments,
                ppfInvestments = entity.ppfInvestments,
                fdInterest = entity.fdInterest,
                rdInterest = entity.rdInterest,
                savingsInterest = entity.savingsInterest,
                investmentReturns = entity.investmentReturns
            )
        }
    }

    val paymentOrders: Flow<List<PaymentOrderEntity>> = database.paymentOrderDao().getAllOrders()

    val emis: Flow<List<EmiItem>> = database.emiDao().getAllEmis().map { list ->
        list.map { entity ->
            EmiItem(
                id = entity.id,
                title = entity.title,
                category = entity.category,
                totalAmount = entity.totalAmount,
                paidAmount = entity.paidAmount,
                monthlyEmi = entity.monthlyEmi,
                interestRate = entity.interestRate,
                totalTenureMonths = entity.totalTenureMonths,
                paidTenureMonths = entity.paidTenureMonths,
                dueDate = entity.dueDate,
                dueDayOfMonth = entity.dueDayOfMonth,
                lenderBank = entity.lenderBank,
                isAutoDebit = entity.isAutoDebit,
                isPaidThisMonth = entity.isPaidThisMonth,
                lastPaymentDate = entity.lastPaymentDate,
                iconName = entity.iconName
            )
        }
    }

    suspend fun addTransaction(
        title: String,
        category: String,
        amount: Double,
        type: String, // EXPENSE, INCOME, TRANSFER
        paymentMethod: String,
        notes: String = "",
        receiptUrl: String? = null,
        isFamilyShared: Boolean = false,
        memberName: String = "Priyanshu"
    ) {
        val isCredit = type == "INCOME"
        database.transactionDao().insertTransaction(
            TransactionEntity(
                title = title,
                category = category,
                amount = amount,
                type = type,
                isCredit = isCredit,
                date = "Today, Just now",
                timestamp = System.currentTimeMillis(),
                paymentMethod = paymentMethod,
                notes = notes,
                receiptUrl = receiptUrl,
                isFamilyShared = isFamilyShared,
                memberName = memberName,
                iconName = when (category.lowercase()) {
                    "food" -> "restaurant"
                    "travel" -> "car"
                    "bills" -> "bolt"
                    "rent" -> "home"
                    "salary" -> "briefcase"
                    "freelance" -> "palette"
                    "shopping" -> "shopping_bag"
                    else -> "wallet"
                }
            )
        )

        // If expense, update budget
        if (!isCredit) {
            database.budgetDao().addSpendingToCategory(category, amount)
        }
    }

    suspend fun deleteTransaction(id: Long) {
        database.transactionDao().deleteTransactionById(id)
    }

    suspend fun addBudget(category: String, monthlyLimit: Double, iconName: String = "category") {
        database.budgetDao().insertBudget(
            BudgetEntity(
                category = category,
                monthlyLimit = monthlyLimit,
                spent = 0.0,
                iconName = iconName
            )
        )
    }

    suspend fun deleteBudget(id: Long) {
        database.budgetDao().deleteBudget(BudgetEntity(id = id, category = "", monthlyLimit = 0.0, spent = 0.0))
    }

    suspend fun addGoal(name: String, emoji: String, targetAmount: Double, targetDate: String, category: String, isFamily: Boolean = false) {
        database.goalDao().insertGoal(
            GoalEntity(
                name = name,
                emoji = emoji,
                targetAmount = targetAmount,
                currentAmount = 0.0,
                targetDate = targetDate,
                category = category,
                isFamilyGoal = isFamily
            )
        )
    }

    suspend fun depositGoal(goalId: Long, amount: Double) {
        database.goalDao().depositToGoal(goalId, amount)
        database.transactionDao().insertTransaction(
            TransactionEntity(
                title = "Goal Deposit: ₹$amount",
                category = "Savings",
                amount = amount,
                type = "TRANSFER",
                isCredit = false,
                date = "Today, Just now",
                timestamp = System.currentTimeMillis(),
                paymentMethod = "Internal Vault Transfer",
                notes = "Milestone allocation"
            )
        )
    }

    suspend fun withdrawGoal(goalId: Long, amount: Double) {
        database.goalDao().withdrawFromGoal(goalId, amount)
    }

    suspend fun deleteGoal(id: Long) {
        database.goalDao().deleteGoal(GoalEntity(id = id, name = "", targetAmount = 0.0, currentAmount = 0.0, targetDate = ""))
    }

    suspend fun addBill(name: String, amount: Double, dueDate: String, category: String, isRecurring: Boolean = true, autoPay: Boolean = false) {
        database.billDao().insertBill(
            BillEntity(
                name = name,
                amount = amount,
                dueDate = dueDate,
                dueTimestamp = System.currentTimeMillis() + 86400000 * 5,
                category = category,
                isRecurring = isRecurring,
                isPaid = false,
                reminderDays = 3,
                autoPayEnabled = autoPay
            )
        )
    }

    suspend fun markBillPaid(billId: Long, billName: String, amount: Double, paymentMethod: String = "UPI") {
        database.billDao().markBillPaymentStatus(billId, true)
        database.transactionDao().insertTransaction(
            TransactionEntity(
                title = "Bill Payment: $billName",
                category = "Bills",
                amount = amount,
                type = "EXPENSE",
                isCredit = false,
                date = "Today, Just now",
                timestamp = System.currentTimeMillis(),
                paymentMethod = paymentMethod,
                notes = "Auto-settled bill invoice",
                iconName = "bolt"
            )
        )
    }

    suspend fun deleteBill(id: Long) {
        database.billDao().deleteBill(BillEntity(id = id, name = "", amount = 0.0, dueDate = "", dueTimestamp = 0))
    }

    suspend fun addFamilyMember(
        name: String,
        role: String,
        email: String,
        monthlyContribution: Double = 0.0,
        spentThisMonth: Double = 0.0,
        salaryIncome: Double = 0.0,
        freelanceIncome: Double = 0.0,
        businessIncome: Double = 0.0,
        rentalIncome: Double = 0.0,
        otherIncome: Double = 0.0,
        foodExpense: Double = 0.0,
        transportExpense: Double = 0.0,
        shoppingExpense: Double = 0.0,
        educationExpense: Double = 0.0,
        healthExpense: Double = 0.0,
        entertainmentExpense: Double = 0.0,
        bankSavings: Double = 0.0,
        emergencyFund: Double = 0.0,
        fixedDeposit: Double = 0.0,
        mutualFund: Double = 0.0,
        monthlyEmi: Double = 0.0,
        equityInvestments: Double = 0.0,
        goldInvestments: Double = 0.0,
        ppfInvestments: Double = 0.0,
        fdInterest: Double = 0.0,
        rdInterest: Double = 0.0,
        savingsInterest: Double = 0.0,
        investmentReturns: Double = 0.0,
        avatarColor: String = "0xFF3B82F6"
    ) {
        database.familyMemberDao().insertMember(
            FamilyMemberEntity(
                name = name,
                role = role,
                email = email,
                avatarColor = avatarColor,
                monthlyContribution = if (monthlyContribution > 0) monthlyContribution else (salaryIncome + businessIncome + freelanceIncome + rentalIncome + otherIncome),
                spentThisMonth = if (spentThisMonth > 0) spentThisMonth else (foodExpense + transportExpense + shoppingExpense + educationExpense + healthExpense + entertainmentExpense),
                salaryIncome = salaryIncome,
                freelanceIncome = freelanceIncome,
                businessIncome = businessIncome,
                rentalIncome = rentalIncome,
                otherIncome = otherIncome,
                foodExpense = foodExpense,
                transportExpense = transportExpense,
                shoppingExpense = shoppingExpense,
                educationExpense = educationExpense,
                healthExpense = healthExpense,
                entertainmentExpense = entertainmentExpense,
                bankSavings = bankSavings,
                emergencyFund = emergencyFund,
                fixedDeposit = fixedDeposit,
                mutualFund = mutualFund,
                monthlyEmi = monthlyEmi,
                equityInvestments = equityInvestments,
                goldInvestments = goldInvestments,
                ppfInvestments = ppfInvestments,
                fdInterest = fdInterest,
                rdInterest = rdInterest,
                savingsInterest = savingsInterest,
                investmentReturns = investmentReturns
            )
        )
    }

    suspend fun updateFamilyMember(member: FamilyMemberItem) {
        database.familyMemberDao().updateMember(
            FamilyMemberEntity(
                id = member.id,
                name = member.name,
                role = member.role,
                email = member.email,
                avatarColor = member.avatarColor,
                monthlyContribution = member.monthlyContribution,
                spentThisMonth = member.spentThisMonth,
                salaryIncome = member.salaryIncome,
                freelanceIncome = member.freelanceIncome,
                businessIncome = member.businessIncome,
                rentalIncome = member.rentalIncome,
                otherIncome = member.otherIncome,
                foodExpense = member.foodExpense,
                transportExpense = member.transportExpense,
                shoppingExpense = member.shoppingExpense,
                educationExpense = member.educationExpense,
                healthExpense = member.healthExpense,
                entertainmentExpense = member.entertainmentExpense,
                bankSavings = member.bankSavings,
                emergencyFund = member.emergencyFund,
                fixedDeposit = member.fixedDeposit,
                mutualFund = member.mutualFund,
                monthlyEmi = member.monthlyEmi,
                equityInvestments = member.equityInvestments,
                goldInvestments = member.goldInvestments,
                ppfInvestments = member.ppfInvestments,
                fdInterest = member.fdInterest,
                rdInterest = member.rdInterest,
                savingsInterest = member.savingsInterest,
                investmentReturns = member.investmentReturns
            )
        )
    }

    suspend fun deleteFamilyMember(id: Long) {
        database.familyMemberDao().deleteMember(
            FamilyMemberEntity(
                id = id,
                name = "",
                role = "",
                email = ""
            )
        )
    }

    suspend fun updateProfile(name: String, email: String, phone: String, currencySymbol: String) {
        val current = database.userProfileDao().getUserProfile()
        database.userProfileDao().insertOrUpdateProfile(
            UserProfileEntity(
                id = 1,
                name = name,
                email = email,
                phone = phone,
                currencySymbol = currencySymbol
            )
        )
    }

    suspend fun addEmi(
        title: String,
        category: String,
        totalAmount: Double,
        paidAmount: Double = 0.0,
        monthlyEmi: Double,
        interestRate: Double = 0.0,
        totalTenureMonths: Int,
        paidTenureMonths: Int = 0,
        dueDate: String = "05th of every month",
        dueDayOfMonth: Int = 5,
        lenderBank: String = "HDFC Bank",
        isAutoDebit: Boolean = true,
        iconName: String = "two_wheeler"
    ) {
        database.emiDao().insertEmi(
            EmiEntity(
                title = title,
                category = category,
                totalAmount = totalAmount,
                paidAmount = paidAmount,
                monthlyEmi = monthlyEmi,
                interestRate = interestRate,
                totalTenureMonths = totalTenureMonths,
                paidTenureMonths = paidTenureMonths,
                dueDate = dueDate,
                dueDayOfMonth = dueDayOfMonth,
                lenderBank = lenderBank,
                isAutoDebit = isAutoDebit,
                isPaidThisMonth = false,
                iconName = iconName
            )
        )
    }

    suspend fun recordEmiPayment(emiId: Long, emiTitle: String, amount: Double, paymentMethod: String = "UPI") {
        database.emiDao().recordEmiPayment(emiId, amount, "Today, Just now")
        database.transactionDao().insertTransaction(
            TransactionEntity(
                title = "EMI Payment: $emiTitle",
                category = "Bills",
                amount = amount,
                type = "EXPENSE",
                isCredit = false,
                date = "Today, Just now",
                timestamp = System.currentTimeMillis(),
                paymentMethod = paymentMethod,
                notes = "Auto-recorded installment settlement",
                iconName = "account_balance"
            )
        )
        // Also update budget for bills
        database.budgetDao().addSpendingToCategory("Bills & Utilities", amount)
    }

    suspend fun updateEmi(emi: EmiEntity) {
        database.emiDao().updateEmi(emi)
    }

    suspend fun deleteEmi(id: Long) {
        database.emiDao().deleteEmiById(id)
    }
}
