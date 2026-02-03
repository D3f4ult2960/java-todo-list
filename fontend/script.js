// Base URL for our backend API
const API = "http://localhost:8080";

/**
 * Fetch all todos from the backend and display them in the table
 */
function fetchTodos() {
  fetch(`${API}/get-todos`)
    .then(res => res.json())
    .then(todos => {
      const tbody = document.getElementById("todoTableBody");
      tbody.innerHTML = ""; // Clear existing rows

      // Loop through each todo and create table rows
      todos.forEach(todo => {
        const tr = document.createElement("tr");

        tr.innerHTML = `
          <td>${todo.id}</td>
          <td>${todo.title}</td>
          <td class="status ${todo.completed ? "done" : "pending"}">
            ${todo.completed ? "Completed" : "Not Completed"}
          </td>
          <td>
            <button class="action-btn toggle-btn"
              onclick="updateTodo(${todo.id})">
              Toggle
            </button>
            <button class="action-btn delete-btn"
              onclick="deleteTodo(${todo.id})">
              Delete
            </button>
          </td>
        `;

        tbody.appendChild(tr);
      });
    })
    .catch(err => console.error("Failed to fetch todos:", err));
}

/**
 * Add a new todo
 */
function addTodo() {
  const input = document.getElementById("todoInput");

  if (!input.value.trim()) return; // Ignore empty input

  fetch(`${API}/add-todo`, {
    method: "POST",
    body: input.value
  })
    .then(() => {
      input.value = "";   // Clear input field
      fetchTodos();       // Refresh table
    })
    .catch(err => console.error("Failed to add todo:", err));
}

/**
 * Toggle todo completion status
 */
function updateTodo(id) {
  fetch(`${API}/update-todo?id=${id}`, {
    method: "POST"
  })
    .then(fetchTodos)
    .catch(err => console.error("Failed to update todo:", err));
}

/**
 * Delete a todo
 */
function deleteTodo(id) {
  fetch(`${API}/delete-todo?id=${id}`, {
    method: "POST"
  })
    .then(fetchTodos)
    .catch(err => console.error("Failed to delete todo:", err));
}

// Initial fetch when page loads
fetchTodos();
