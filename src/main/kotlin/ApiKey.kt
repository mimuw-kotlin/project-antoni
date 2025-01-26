import io.github.cdimascio.dotenv.dotenv

val dotenv = dotenv()
val apiKey = dotenv["API_KEY"] ?: throw IllegalStateException("API_KEY not found in .env file")