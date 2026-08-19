import { StyleSheet } from "react-native";

export const commonStyles = StyleSheet.create({
  // login and registration fields
  container: { 
    flex: 1, 
    justifyContent: "center", 
    alignItems: "center", 
    padding: 16
  },
  form: { 
    width: "100%", 
    borderWidth: 1, 
    borderColor: "#d1d5db", 
    borderRadius: 6, 
    padding: 16 
  },
  input: { 
    borderWidth: 1, 
    borderColor: "#d1d5db", 
    borderRadius: 4, 
    paddingHorizontal: 12, 
    paddingVertical: 8, 
    marginTop: 4, 
    marginBottom: 16, 
    color: "#000000" 
  },
  buttonRow: { 
    flexDirection: "row", 
    justifyContent: "space-between", 
    alignItems: "center" 
  },

  // text
  title: { fontSize: 28, fontWeight: "bold", marginBottom: 4 },
  subtitle: { fontSize: 18, marginBottom: 16 },

  // buttons
  button: {
    alignSelf: "flex-start",
    height: 36,
    borderRadius: 10,
    boxShadow: "0px 0px 5px rgb(92, 195, 255)",
    backgroundColor: "rgb(92, 195, 255)",
    justifyContent: "center",
    alignItems: "center",
    paddingHorizontal: 10,
  },
  buttonPressed: {
    boxShadow: "0px",
    backgroundColor: "rgb(144, 214, 255)",
  },
  buttonText: {
    color: "white",
    fontSize: 14,
    fontWeight: "600",
  },
  deleteButton: {
    borderWidth: 1,
    borderColor: "rgb(30, 90, 150)",
  },
  deleteButtonPressed: {
    backgroundColor: "rgba(30, 90, 150, .15)",
  },
  deleteButtonText: {   // separate from buttonText
    color: "rgb(30, 90, 150)",
    fontWeight: "bold",
  },

  // error handling
  errorBox: {
    borderWidth: 1, 
    borderColor: "#f87171", 
    backgroundColor: "#fee2e2", 
    borderRadius: 6, 
    padding: 12, 
    marginBottom: 12, 
    width: "100%" 
  },
  errorText: { 
    color: "#b91c1c" 
  },
});
