package com.example.lab_week_09

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
// PERBAIKAN 2: Import Moshi
import com.example.lab_week_09.ui.theme.LAB_WEEK_09Theme
import com.example.lab_week_09.ui.theme.OnBackgroundItemText
import com.example.lab_week_09.ui.theme.OnBackgroundTitleText
import com.example.lab_week_09.ui.theme.PrimaryTextButton
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class MainActivity : ComponentActivity() {

    // PERBAIKAN 2: Tambahkan anotasi @JsonClass agar Moshi bisa mengenalinya
    @JsonClass(generateAdapter = true)
    data class Student(
        var name: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LAB_WEEK_09Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    App(
                        navController = navController
                    )
                }
            }
        }
    }
}

@Composable
fun App(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            Home { listDataJson ->
                // Navigasi dengan string JSON
                // Catatan: Navigasi akan otomatis URL-encode string ini
                navController.navigate(
                    "resultContent/?listData=$listDataJson"
                )
            }
        }
        composable(
            "resultContent/?listData={listData}",
            arguments = listOf(navArgument("listData") {
                type = NavType.StringType
            })
        ) {
            // Navigasi akan otomatis URL-decode string JSON di sini
            ResultContent(
                it.arguments?.getString("listData").orEmpty()
            )
        }
    }
}


@Composable
fun Home(navigateFromHomeToResult: (String) -> Unit) {

    val listData = remember {
        mutableStateListOf(
            MainActivity.Student("Tanu"),
            MainActivity.Student("Tina"),
            MainActivity.Student("Tono")
        )
    }

    val inputField = remember { mutableStateOf(MainActivity.Student("")) }

    // PERBAIKAN 2: Siapkan Moshi untuk konversi
    val moshi = remember {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }
    val listType = Types.newParameterizedType(List::class.java, MainActivity.Student::class.java)
    val jsonAdapter: JsonAdapter<List<MainActivity.Student>> = remember { moshi.adapter(listType) }


    HomeContent(
        listData,
        inputField.value,
        { input -> inputField.value = inputField.value.copy(name = input) },

        // PERBAIKAN 1: Validasi string kosong
        {
            if (inputField.value.name.isNotBlank()) {
                listData.add(inputField.value)
                inputField.value = MainActivity.Student("")
            }
        },

        // PERBAIKAN 2: Konversi list ke JSON sebelum navigasi
        {
            val jsonString = jsonAdapter.toJson(listData.toList())
            navigateFromHomeToResult(jsonString)
        }
    )
}


@Composable
fun HomeContent(
    listData: SnapshotStateList<MainActivity.Student>,
    inputField: MainActivity.Student,
    onInputValueChange: (String) -> Unit,
    onButtonClick: () -> Unit,
    navigateFromHomeToResult: () -> Unit
) {
    LazyColumn {
        item {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OnBackgroundTitleText(
                    text = stringResource(
                        id = R.string.enter_item
                    )
                )
                TextField(
                    value = inputField.name,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text
                    ),
                    onValueChange = {
                        onInputValueChange(it)
                    }
                )
                Row {
                    PrimaryTextButton(
                        text = stringResource(
                            id =
                                R.string.button_click
                        )
                    ) {
                        onButtonClick()
                    }
                    PrimaryTextButton(
                        text = stringResource(
                            id = R.string.button_navigate
                        )
                    ) {
                        navigateFromHomeToResult()
                    }
                }
            }
        }
        items(listData) { item ->
            Column(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OnBackgroundItemText(text = item.name)
            }
        }
    }
}

// PERBAIKAN 2: 'ResultContent' di-update untuk parse JSON dan pakai LazyColumn
@Composable
fun ResultContent(listDataJson: String) {

    // 1. Siapkan Moshi untuk parsing
    val moshi = remember {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }
    val listType = Types.newParameterizedType(List::class.java, MainActivity.Student::class.java)
    val jsonAdapter: JsonAdapter<List<MainActivity.Student>> = remember { moshi.adapter(listType) }

    // 2. Parse JSON. Gunakan 'try-catch' untuk keamanan
    val studentList: List<MainActivity.Student> = remember(listDataJson) {
        try {
            if (listDataJson.isNotBlank()) {
                jsonAdapter.fromJson(listDataJson) ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList() // Kembalikan list kosong jika JSON rusak
        }
    }

    // 3. Tampilkan dengan LazyColumn
    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            OnBackgroundTitleText(text = "Hasil List (dari JSON)")
        }

        if (studentList.isEmpty()) {
            item {
                OnBackgroundItemText(text = "Tidak ada data untuk ditampilkan.")
            }
        } else {
            items(studentList) { student ->
                Column(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OnBackgroundItemText(text = student.name)
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewHome() {
    Home(navigateFromHomeToResult = {})
}