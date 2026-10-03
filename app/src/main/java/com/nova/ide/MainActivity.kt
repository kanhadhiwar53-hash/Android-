package com.nova.ide

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nova.ide.core.ProjectManager
import com.nova.ide.core.ProjectScanner
import com.nova.ide.databinding.ActivityMainBinding
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var ui: ActivityMainBinding
    private lateinit var manager: ProjectManager
    private var project: File? = null
    private var openedFile: File? = null
    private val fileItems = mutableListOf<File>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)
        manager = ProjectManager(this)
        ui.newProjectButton.setOnClickListener { createProject() }
        ui.openProjectButton.setOnClickListener { chooseProject() }
        ui.refreshButton.setOnClickListener { refreshFiles() }
        ui.saveButton.setOnClickListener { saveFile() }
        ui.buildButton.setOnClickListener { inspectBuild() }
        ui.editorText.setText("// Welcome to NOVA IDE V0.1\n// Create or open a project.")
        ui.editorText.isEnabled = false
    }

    private fun createProject() {
        val field = EditText(this).apply { hint = "MyFirstApp" }
        AlertDialog.Builder(this).setTitle("Create Android project").setView(field)
            .setPositiveButton("Create") { _, _ ->
                runCatching { manager.createAndroidProject(field.text.toString().trim()) }
                    .onSuccess { openProject(it); ui.consoleText.text = "Created ${it.name}" }
                    .onFailure { showError(it.message ?: "Project creation failed") }
            }.setNegativeButton("Cancel", null).show()
    }

    private fun chooseProject() {
        val all = manager.getProjects()
        if (all.isEmpty()) { toast("No projects. Create one first."); return }
        AlertDialog.Builder(this).setTitle("Open project")
            .setItems(all.map { it.name }.toTypedArray()) { _, i -> openProject(all[i]) }
            .setNegativeButton("Cancel", null).show()
    }

    private fun openProject(dir: File) {
        project = dir
        openedFile = null
        ui.projectTitle.text = dir.name
        ui.currentFileLabel.text = "Select a file"
        ui.editorText.setText("// Select a file in the explorer")
        ui.editorText.isEnabled = false
        refreshFiles()
    }

    private fun refreshFiles() {
        val root = project ?: return
        fileItems.clear()
        fileItems.addAll(root.walkTopDown().filter { it.isFile }.sortedBy { it.relativeTo(root).path }.take(250))
        ui.fileList.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
            fileItems.map { it.relativeTo(root).invariantSeparatorsPath })
        ui.fileList.setOnItemClickListener { _, _, index, _ -> openFile(fileItems[index]) }
    }

    private fun openFile(file: File) {
        if (file.length() > 1_000_000L) { showError("File is too large for the basic editor"); return }
        val contents = runCatching { file.readText() }.getOrElse { showError("Cannot read file"); return }
        openedFile = file
        ui.currentFileLabel.text = file.relativeTo(project!!).invariantSeparatorsPath
        ui.editorText.isEnabled = true
        ui.editorText.setText(contents)
    }

    private fun saveFile() {
        val file = openedFile ?: run { toast("Open a file first"); return }
        runCatching { file.writeText(ui.editorText.text.toString()) }
            .onSuccess { ui.consoleText.text = "Saved ${file.name}"; toast("Saved") }
            .onFailure { showError(it.message ?: "Save failed") }
    }

    private fun inspectBuild() {
        val root = project ?: run { showError("Open a project first"); return }
        ui.consoleText.text = "Inspecting project..."
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) { ProjectScanner().scan(root) }
            ui.consoleText.text = "NOVA Build Engine V0.1\nProject: ${result.name}\nGradle: ${result.hasGradle}\nKotlin: ${result.hasKotlin}\nJava: ${result.hasJava}\nManifest: ${result.hasManifest}\n\nAPK compilation is not integrated yet."
        }
    }

    private fun showError(message: String) { ui.consoleText.text = "Error: $message"; toast(message) }
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}
