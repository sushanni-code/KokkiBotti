KokkiBotti: A Modern Android Recipe & Meal Planning App

KokkiBotti is a comprehensive recipe management and meal-planning application developed for Android. This project was built as a final assignment for a Mobile Programming course, demonstrating a modern, feature-rich architecture using Kotlin and the latest Android Jetpack libraries.
The application allows users to discover, save, and organize recipes, as well as plan their meals for up to four weeks. A key feature is the "Inspiration" section, which integrates with the public TheMealDB API to fetch random meal ideas that can be directly saved into the user's local collection.

Core Features
• Recipe Management: Create, read, update, and delete (CRUD) personal recipes with detailed ingredients and instructions.
• Meal Planner: Plan meals for a full four-week schedule. Users can assign saved recipes to each day.
• Recipe Discovery: An "Inspiration" feature that fetches random recipes from the external TheMealDB API.
• Dynamic Theming: A settings option to switch between a light and a dark theme, with the user's preference saved across sessions.
• Intuitive UI: A modern user interface built with MaterialCardView, DrawerLayout, and BottomNavigationView for seamless navigation.

Technical Architecture
This project is built upon a Single-Activity Architecture using the Model-View-ViewModel (MVVM) pattern to create a scalable and maintainable codebase.
• View / UI Layer:
◦ MainActivity serves as the single entry point, hosting a NavHostFragment.
◦ UI is composed of multiple Fragments (RecipesFragment, PlannerFragment, InspirationFragment, etc.) managed by the Android Navigation Component.
◦ Layouts are built with ConstraintLayout, MaterialCardView, and RecyclerView for efficient and responsive displays.
• ViewModel Layer:
◦ A shared RecipeViewModel is scoped to the MainActivity to provide a single source of truth for all recipe and meal plan data.
◦ It exposes data to the UI using LiveData, ensuring the UI is always in sync with the underlying data.
◦ All database operations are executed within viewModelScope coroutines to prevent blocking the main thread.
• Model / Data Layer:
◦ Room Persistence Library: Serves as the local database for storing all user-created recipes and meal plans.
▪ Entities: Recipe and PlannedMeal classes define the database schemas.
▪ DAOs (Data Access Objects): RecipeDao and PlannedMealDao provide abstract interfaces for all database queries.
▪ Type Converters: A custom TypeConverter is used to serialize and deserialize the List<Ingredient> for storage in the Room database.
◦ Retrofit & Gson: Used for networking to communicate with TheMealDB API.
◦ Coil: For efficient loading and caching of recipe images from the network.

Key Libraries & Components
• Jetpack:
◦ Room: For robust, local SQL database storage.
◦ Navigation Component: For handling all in-app navigation.
◦ ViewModel & LiveData: For managing UI-related data in a lifecycle-aware way.
◦ Fragment & AppCompat: Core UI components.
• Material Design: For modern UI components like MaterialCardView, BottomNavigationView, and DrawerLayout.
• Retrofit: For type-safe HTTP client integration.
• KSP (Kotlin Symbol Processing): Used by Room for annotation processing.

Development Note
This application was developed with the assistance of artificial intelligence for bootstrapping components, debugging, and architectural guidance, serving as a case study in AI-assisted mobile development.
