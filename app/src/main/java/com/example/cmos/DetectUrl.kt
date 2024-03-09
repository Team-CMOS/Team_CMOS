package com.example.cmos

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.jsoup.Jsoup

@Preview(showBackground = true)
@Composable
fun HomeScreen() {
    var websiteUrl by remember { mutableStateOf("") }
    var webViewUrl by remember { mutableStateOf("") }
    var webViewVisible by remember { mutableStateOf(false) }


    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
            .shadow(1.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BasicTextField(
                value = websiteUrl,
                onValueChange = { websiteUrl = it },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        // Handle Done action, e.g., initiate dark pattern detection
                        // using the entered website URL
                        webViewUrl = websiteUrl
                        webViewVisible = true
                    }
                ),
                textStyle = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.background)
                    .clip(MaterialTheme.shapes.medium)
                    .padding(16.dp)
            )

            if (webViewVisible) {
                WebViewing(webViewUrl)
            }

            Button(
                onClick = {
                    if (!webViewVisible) {
                        // Only initiate WebView if it's not already visible
                        webViewUrl = websiteUrl
                        webViewVisible = true
                    }
                },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Detect Dark Patterns")
            }

        }
    }
}

@Composable
fun WebViewing(url : String){
    AndroidView(factory = {
        WebView(it).apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
            loadUrl(url)

            // After the page loads, extract HTML content
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    extractHtmlContent()
                }
            }
        }
    })
}

private fun WebView.extractHtmlContent() {
    evaluateJavascript("(function() { return document.documentElement.outerHTML; })();") { html ->
        val cleanedHtml = Jsoup.parse(html).text() // Remove HTML tags
        // Process the cleaned HTML content as needed
        // For demonstration, let's just log it:
        println("Extracted HTML: $cleanedHtml")
    }
}
