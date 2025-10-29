package com.example.repairstoremanager.ui.components.customer.add

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.repairstoremanager.data.model.Customer
import com.example.repairstoremanager.ui.components.common.FormSpinner
import com.example.repairstoremanager.ui.components.customer.invoice.InvoicePrintBottomSheet
import com.example.repairstoremanager.viewmodel.CustomerViewModel
import com.example.repairstoremanager.viewmodel.StoreViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.S)
@SuppressLint("SimpleDateFormat")
@Composable
fun QuickInvoiceForm(
    modifier: Modifier = Modifier,
    onClose: () -> Unit = {},
    onSaveSuccess: () -> Unit = {}
) {
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
    var isLoading by remember { mutableStateOf(false) }
    var showPrintSheet by remember { mutableStateOf(false) }
    var currentCustomer by remember { mutableStateOf<Customer?>(null) }
    val userPhoneModels by viewModel.userPhoneModels.collectAsState()
    val userProblems by viewModel.userProblems.collectAsState()
    var isSaved by remember { mutableStateOf(false) }

    // Validation state
    val isFormValid = remember(customerName, contactNumber) {
        customerName.trim().isNotEmpty() || contactNumber.trim().isNotEmpty()
    }

        val currentInvoiceNumber by viewModel.currentInvoiceNumber.collectAsState()
        val deviceStatusOptions = listOf("Pending", "Repaired", "Delivered", "Cancel")
        var deviceStatus by remember { mutableStateOf("Pending") }

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

        fun clearForm() {
            customerName = ""
            contactNumber = ""
            phoneModel = ""
            problem = ""
            deliveryDate = ""
            totalAmount = ""
            advanced = ""
            deviceStatus = "Pending"
        }

        fun saveCustomer(showPrintAfterSave: Boolean = false) {
            if (isLoading) return // Prevent multiple saves

            if (customerName.isBlank() || contactNumber.isBlank()) {
                Toast.makeText(context, "Please enter customer details", Toast.LENGTH_SHORT).show()
                return
            }

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
                securityType = "",
                phonePassword = "",
                pattern = emptyList(),
                battery = false,
                sim = false,
                memory = false,
                simTray = false,
                backCover = false,
                deadPermission = false,
                status = deviceStatus,
            )

            viewModel.addCustomer(
                newCustomer,
                onSuccess = { savedCustomer ->
                    isLoading = false
                    Toast.makeText(context, "Invoice created!", Toast.LENGTH_SHORT).show()
                    currentCustomer = savedCustomer
                    if (showPrintAfterSave) {
                        showPrintSheet = true
                    }
                    onSaveSuccess()
                    clearForm()
                },
                onError = {
                    isLoading = false
                    Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                }
            )
        }

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
            // Customer Information
//            CustomerInfoSection(
//                customerName = customerName,
//                contactNumber = contactNumber,
//                isFormValid = isFormValid,
//                onCustomerNameChange = { customerName = it },
//                onContactNumberChange = { contactNumber = it }
//            )

            Spacer(modifier = Modifier.height(16.dp))

            // Device Information
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

            Spacer(modifier = Modifier.height(8.dp))

            // Status Selection (new addition)
            FormSpinner(
                label = "Status",
                selectedValue = deviceStatus,
                options = deviceStatusOptions,
                onValueChange = { deviceStatus = it },
                modifier = Modifier.fillMaxWidth()
            )


            Spacer(modifier = Modifier.height(16.dp))

            // Payment Information
            PaymentInfoSection(
                totalAmount = totalAmount,
                advanced = advanced,
                onTotalAmountChange = { totalAmount = it },
                onAdvancedChange = { advanced = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            SaveCustomerButton(
                isLoading = isLoading,
                isFormValid = isFormValid,
                onSaveClick = { saveCustomer(false) },
                onSaveAndPrintClick = { saveCustomer(true) },
                onPreviewClick = { showPrintSheet = true },
                isSaved = isSaved
            )
            Spacer(modifier = Modifier.height(40.dp))
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