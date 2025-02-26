package com.example.plu_search_v2_app

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.plu_search_v2_app.ui.theme.Plusearchv2appTheme

// Data class representing a product entry.
data class ProduceItem(
    val description: String,
    val plu: String
)

class MainActivity : ComponentActivity() {

    // List to hold our products loaded from the CSV.
    private lateinit var products: List<ProduceItem>

    // A mutable state holding the current list of search results.
    private var searchResults by mutableStateOf<List<ProduceItem>>(emptyList())

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Load product data from the CSV file in assets.
        products = loadProduceItems()

        // Register a launcher for voice recognition.
        val voiceRecognitionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Retrieve the recognized text.
                val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (!matches.isNullOrEmpty()) {
                    val voiceInput = matches[0]
                    // Look up the top 5 matching products based on the recognized text.
                    val results = lookupTopPlu(voiceInput)
                    // Update the UI state with the new results.
                    searchResults = results

                    // Optionally, display a Toast if no results are found.
                    if (results.isEmpty()) {
                        Toast.makeText(this, "No match found", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Set up the Compose UI.
        setContent {
            Plusearchv2appTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    // All composable calls are now inside the setContent lambda.
                    SearchScreen(
                        onSpeakButtonClick = {
                            // Clear previous results.
                            searchResults = emptyList()

                            // Create and launch the voice recognition intent.
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(
                                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                )
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
                            }
                            voiceRecognitionLauncher.launch(intent)
                        },
                        onTextSearch = { searchText ->
                            // Clear previous results.
                            searchResults = emptyList()

                            // Perform search using the text input
                            val results = lookupTopPlu(searchText)
                            searchResults = results

                            // Display a Toast if no results are found.
                            if (results.isEmpty()) {
                                Toast.makeText(this, "No match found", Toast.LENGTH_LONG).show()
                            }
                        },
                        searchResults = searchResults
                    )
                }
            }
        }
    }

    /**
     * Loads the CSV file from the assets folder and parses it into a list of ProduceItem.
     */
    private fun loadProduceItems(fileName: String = "coborn_produce_plu_simplified_v2.csv"): List<ProduceItem> {
        val productList = mutableListOf<ProduceItem>()
        try {
            assets.open(fileName).bufferedReader().use { reader ->
                val lines = reader.readLines()
                // Skip the header line.
                lines.drop(1).forEach { line ->
                    val tokens = line.split(",")
                    if (tokens.size >= 2) {
                        val plu = tokens[0].trim()
                        val description = tokens[1].trim()
                        productList.add(ProduceItem(description, plu))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error loading CSV: ${e.message}", Toast.LENGTH_LONG).show()
        }
        return productList
    }

    /**
     * Returns the top 5 matching products based on the voice input.
     */
    private fun lookupTopPlu(searchTerm: String): List<ProduceItem> {
        // Convert search term to lowercase and split into keywords.
        val keywords = searchTerm.lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }

        // Map each product to a Pair<ProduceItem, score> if it qualifies.
        val matchingProducts = products.mapNotNull { product ->
            // Split product description into words.
            val descriptionWords = product.description.lowercase().split("\\W+".toRegex())

            // For each keyword, find the best matching distance from any description word.
            val scores = keywords.mapNotNull { keyword ->
                // Compute the minimum distance for this keyword.
                val bestDistance = descriptionWords.map { word -> levenshtein(keyword, word) }
                    .minOrNull() ?: Int.MAX_VALUE
                // Set allowed threshold: 1 edit for short keywords, 2 for longer keywords.
                val threshold = if (keyword.length <= 4) 1 else 2
                if (bestDistance <= threshold) bestDistance else null
            }
            // Only consider the product if every keyword was matched.
            if (scores.size == keywords.size) {
                val totalScore = scores.sum()
                Pair(product, totalScore)
            } else {
                null
            }
        }
        // Sort products by total score (lower is better) and take the top 5.
        return matchingProducts.sortedBy { it.second }
            .map { it.first }
            .take(5)
    }

    /**
     * Computes the Levenshtein distance between two strings.
     */
    private fun levenshtein(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }

        // Initialize the first column and row.
        for (i in 0..a.length) {
            dp[i][0] = i
        }
        for (j in 0..b.length) {
            dp[0][j] = j
        }

        // Compute distances using dynamic programming.
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
        return dp[a.length][b.length]
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    onSpeakButtonClick: () -> Unit,
    onTextSearch: (String) -> Unit,
    searchResults: List<ProduceItem>
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var searchText by remember { mutableStateOf("") }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // A large Speak button.
        Button(
            onClick = onSpeakButtonClick,
            modifier = Modifier
                .padding(top = 16.dp, bottom = 16.dp)
                .fillMaxWidth()
                .height(80.dp)
        ) {
            Text(text = "Speak", fontSize = 24.sp)
        }

        // Text input field
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Enter produce name") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onTextSearch(searchText)
                    keyboardController?.hide()
                }
            )
        )

        // Search button
        Button(
            onClick = {
                onTextSearch(searchText)
                keyboardController?.hide()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(vertical = 8.dp)
        ) {
            Text("Search", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the search results (if any).
        if (searchResults.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                searchResults.forEach { product ->
                    Text(
                        text = "${product.description} (PLU: ${product.plu})",
                        fontSize = 24.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}