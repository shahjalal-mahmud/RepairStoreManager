package com.example.repairstoremanager.ui.components.customer.add

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.ui.components.customer.common.AccessoriesSection
import com.example.repairstoremanager.ui.components.customer.invoice.InvoicePrintBottomSheet
import com.example.repairstoremanager.ui.components.customer.media.CaptureMediaSection
import com.example.repairstoremanager.viewmodel.CustomerViewModel
import com.example.repairstoremanager.viewmodel.StoreViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.S)
@SuppressLint("SimpleDateFormat")
@Composable
fun InvoiceFormSection(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    val date = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a").format(Date()) }
    val context = LocalContext.current
    val viewModel: CustomerViewModel = viewModel()
    val storeViewModel: StoreViewModel = viewModel()

    // Form state
    var customerName by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var phoneModel by remember { mutableStateOf("") }
    var problem by remember { mutableStateOf("") }
    var deliveryDate by remember { mutableStateOf("") }
    var totalAmount by remember { mutableStateOf("") }
    var advanced by remember { mutableStateOf("") }
    var securityType by remember { mutableStateOf("Password") }
    var phonePassword by remember { mutableStateOf("") }
    var pattern by remember { mutableStateOf(listOf<Int>()) }
    var battery by remember { mutableStateOf(false) }
    var sim by remember { mutableStateOf(false) }
    var memory by remember { mutableStateOf(false) }
    var simTray by remember { mutableStateOf(false) }
    var backCover by remember { mutableStateOf(false) }
    var deadPermission by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var hasDrawnPattern by remember { mutableStateOf(false) }
    var patternResetKey by remember { mutableIntStateOf(0) }
    var currentCustomer by remember { mutableStateOf<Customer?>(null) }
    var showPrintSheet by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }
    val userPhoneModels by viewModel.userPhoneModels.collectAsState()
    val userProblems by viewModel.userProblems.collectAsState()

    val currentInvoiceNumber by viewModel.currentInvoiceNumber.collectAsState()

    // Common phone models
    val commonPhoneModels = remember {
        setOf(
            "Samsung", "iPhone", "Xiaomi", "Itel", "Symphony",
            "Walton", "Vivo", "Oppo", "Tecno", "Realme",
            "OnePlus", "Nokia", "Huawei", "Motorola", "LG"
        )
    }

    // Common problems
    val commonProblems = remember {
        setOf(
            "IC", "Network", "Display",
            "Charging Port(C)", "Charging Port(Lightning)",
            "Charging Port(B)", "Body Frame",
            "Keypad Switch", "Slide Switch", "Flashing",
            "FRP Unlocking", "Screen Unlocking", "Battery",
            "Water Damage", "Speaker Issue", "Microphone Issue"
        )
    }

    LaunchedEffect(Unit) {
        viewModel.fetchNextInvoiceNumber()
    }

    val calendar = remember { Calendar.getInstance() }
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                deliveryDate = sdf.format(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun clearForm() {
        customerName = ""
        contactNumber = ""
        phoneModel = ""
        problem = ""
        deliveryDate = ""
        totalAmount = ""
        advanced = ""
        securityType = "Password"
        phonePassword = ""
        pattern = emptyList()
        battery = false
        sim = false
        memory = false
        simTray = false
        backCover = false
        deadPermission = false
        hasDrawnPattern = false
        patternResetKey++
        isSaved = false
    }

    fun saveCustomer(showPrintAfterSave: Boolean = false) {
        if (isLoading) return // Prevent multiple saves

        isLoading = true
        val newCustomer = Customer(
            id = "",
            shopOwnerId = "",
            invoiceNumber = currentInvoiceNumber ?: "",
            date = date,
            customerName = customerName,
            contactNumber = contactNumber,
            phoneModel = phoneModel,
            problem = problem,
            deliveryDate = deliveryDate,
            totalAmount = totalAmount,
            advanced = advanced,
            securityType = securityType,
            phonePassword = phonePassword,
            pattern = pattern,
            battery = battery,
            sim = sim,
            memory = memory,
            simTray = simTray,
            backCover = backCover,
            deadPermission = deadPermission,
            status = "Pending",
        )

        viewModel.addCustomer(
            newCustomer,
            onSuccess = { savedCustomer ->
                isLoading = false
                Toast.makeText(context, "Customer saved!", Toast.LENGTH_SHORT).show()
                currentCustomer = savedCustomer
                isSaved = true
                if (showPrintAfterSave) {
                    showPrintSheet = true
                }
                clearForm()
            },
            onError = {
                isLoading = false
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        )
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(context, "Please allow SMS permission", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            currentInvoiceNumber?.let { invoiceNum ->
                Text(
                    text = "Invoice #: $invoiceNum",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            CustomerInfoSection(
                customerName = customerName,
                contactNumber = contactNumber,
                onCustomerNameChange = { customerName = it },
                onContactNumberChange = { contactNumber = it }
            )

            Spacer(Modifier.height(20.dp))

            DeviceInfoSection(
                phoneModel = phoneModel,
                problem = problem,
                deliveryDate = deliveryDate,
                onPhoneModelChange = { phoneModel = it },
                onProblemChange = { problem = it },
                onAddPhoneModel = { viewModel.addUserPhoneModel(it) },
                onAddProblem = { viewModel.addUserProblem(it) },
                onDeliveryDateClick = { datePickerDialog.show() },
                commonPhoneModels = commonPhoneModels,
                userPhoneModels = userPhoneModels,
                commonProblems = commonProblems,
                userProblems = userProblems
            )

            Spacer(Modifier.height(20.dp))

            PaymentInfoSection(
                totalAmount = totalAmount,
                advanced = advanced,
                onTotalAmountChange = { totalAmount = it },
                onAdvancedChange = { advanced = it }
            )

            Spacer(Modifier.height(20.dp))

            SecurityInfoSection(
                securityType = securityType,
                phonePassword = phonePassword,
                pattern = pattern,
                hasDrawnPattern = hasDrawnPattern,
                patternResetKey = patternResetKey,
                onSecurityTypeChange = { securityType = it },
                onPasswordChange = { phonePassword = it },
                onPatternComplete = { drawn ->
                    if (drawn.size >= 4) {
                        pattern = drawn
                        hasDrawnPattern = true
                    }
                },
                onResetPattern = {
                    pattern = emptyList()
                    hasDrawnPattern = false
                    patternResetKey++
                }
            )
            Spacer(Modifier.height(20.dp))

            CaptureMediaSection(customerId = currentInvoiceNumber ?: "temp")

            Spacer(Modifier.height(20.dp))

            AccessoriesSection(
                battery = battery,
                sim = sim,
                memory = memory,
                simTray = simTray,
                backCover = backCover,
                deadPermission = deadPermission,
                onBatteryChange = { battery = it },
                onSimChange = { sim = it },
                onMemoryChange = { memory = it },
                onSimTrayChange = { simTray = it },
                onBackCoverChange = { backCover = it },
                onDeadPermissionChange = { deadPermission = it }
            )

            Spacer(Modifier.height(24.dp))

            SaveCustomerButton(
                isLoading = isLoading,
                onSaveClick = { saveCustomer(false) },
                onSaveAndPrintClick = { saveCustomer(true) },
                onPreviewClick = { showPrintSheet = true },
                isSaved = isSaved
            )

            Spacer(Modifier.height(40.dp))
        }

        if (showPrintSheet && currentCustomer != null) {
            InvoicePrintBottomSheet(
                customer = currentCustomer!!.copy(
                    invoiceNumber = currentCustomer!!.invoiceNumber.ifEmpty {
                        viewModel.currentInvoiceNumber.value ?: "INV-000000"
                    }
                ),
                storeInfo = storeViewModel.storeInfo,
                onDismiss = { showPrintSheet = false }
            )
        }
    }
}