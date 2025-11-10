package com.example.lab_week_09


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
// import androidx.activity.enableEdgeToEdge // Tidak terpakai
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
// import androidx.compose.material3.Button // Diganti
import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.Scaffold // Tidak terpakai
import androidx.compose.material3.Surface
// import androidx.compose.material3.Text // Diganti
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
import com.example.lab_week_09.ui.theme.LAB_WEEK_09Theme
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lab_week_09.ui.theme.OnBackgroundItemText
import com.example.lab_week_09.ui.theme.OnBackgroundTitleText
import com.example.lab_week_09.ui.theme.PrimaryTextButton


//Previously we extend AppCompatActivity,
//now we extend ComponentActivity
class MainActivity : ComponentActivity() {

    //Declare a data class called Student
    data class Student(
        var name: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Here, we use setContent instead of setContentView
        setContent {
            //Here, we wrap our content with the theme
            //You can check out the LAB_WEEK_09Theme inside Theme.kt
            LAB_WEEK_09Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    //We use Modifier.fillMaxSize() to make the surface fill the whole screen
                    modifier = Modifier.fillMaxSize(),
                    //We use MaterialTheme.colorScheme.background to get the background color
                    //and set it as the color of the surface
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

//Here, we create a composable function called App
//This will be the root composable of the app
@Composable
fun App(navController: NavHostController) {
    //Here, we use NavHost to create a navigation graph
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        //Here, we create a route called "home"
        composable("home") {
            //Here, we pass a lambda function that navigates to "resultContent"
            Home { navController.navigate(
                "resultContent/?listData=$it")
            }
        }
        //Here, we create a route called "resultContent"
        composable(
            "resultContent/?listData={listData}",
            arguments = listOf(navArgument("listData") {
                type = NavType.StringType }
            )
        ) {
            //Here, we pass the value of the argument to the ResultContent composable
            // PERBAIKAN: 'ResultContent' sekarang bisa dipanggil
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

    // PERBAIKAN (Error 2): Tipe datanya adalah 'MutableState<Student>'
    // Kita biarkan Kotlin menebaknya (type inference)
    val inputField = remember { mutableStateOf(MainActivity.Student("")) }


    HomeContent(
        listData,
        // PERBAIKAN: Kirim 'isinya' (.value)
        inputField.value,
        // PERBAIKAN (Error 3): Ubah 'isinya' (.value)
        { input -> inputField.value = inputField.value.copy(name = input) },
        // PERBAIKAN (Error 4):
        {
            // Tambahkan 'isinya' (.value)
            listData.add(inputField.value)
            // Reset 'isinya' (.value)
            inputField.value = MainActivity.Student("")
        },
        { navigateFromHomeToResult(listData.toList().toString()) }
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
    //Here, we use LazyColumn to display a list of items lazily
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
                            id = R.string.button_navigate // Pastikan string 'button_navigate' ada di strings.xml
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
} // <-- Kurung tutup untuk HomeContent SEHARUSNYA DI SINI

// PERBAIKAN (Error 1): 'ResultContent' dipindah ke top-level
@Composable
fun ResultContent(listData: String) {
    Column(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //Here, we call the OnBackgroundItemText UI Element
        OnBackgroundItemText(text = listData)
    }
}


// PERBAIKAN (Error 5): 'PreviewHome' dipindah ke top-level
@Preview(showBackground = true)
@Composable
fun PreviewHome() {
    Home(navigateFromHomeToResult = {})
}