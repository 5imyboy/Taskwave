import { Link, useRouter } from "expo-router";
import { useState } from "react";
import { useAuth } from "../contexts/AuthContext";
import { KeyboardAvoidingView, Platform, Pressable, Text, TextInput, View } from "react-native";
import { commonStyles } from "../styles/common";

export default function Login() {

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const router = useRouter();
  const { setToken } = useAuth();
  const [errors, setErrors] = useState([]);
  const url = `${process.env.EXPO_PUBLIC_API_URL}/login`;

  const handleSubmit = async () => {
    try {
      const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      if (response.status !== 200 && response.status !== 401) {
        throw new Error(`Unexpected status: ${response.status}`);
      }
      const data = await response.json();
      if (data.token) {
        setToken(data.token);
        router.back();
      } else {
        setErrors(data);
      }
    } catch (e) {
      console.log(e);
    }
  }

  return (
    <KeyboardAvoidingView
      style={{ flex: 1 }}
      behavior={Platform.OS === "ios" ? "padding" : "height"}
    >
      <View style={commonStyles.container}>
        <Text style={commonStyles.title}>Taskwave</Text>
        <Text style={commonStyles.subtitle}>Login:</Text>

        {errors.length > 0 && (
          <View style={commonStyles.errorBox}>
            {errors.map(e => <Text key={e} style={commonStyles.errorText}>{e}</Text>)}
          </View>
        )}

        <View style={commonStyles.form}>
          <Text>Email address:</Text>
          <TextInput
            style={commonStyles.input}
            id="email"
            onChangeText={setEmail}
            keyboardType="email-address"
            autoCapitalize="none"
            defaultValue={email}
          />
          <Text>Password:</Text>
          <TextInput
            style={commonStyles.input}
            id="password"
            value={password}
            onChangeText={setPassword}
            secureTextEntry
          />
          <View style={commonStyles.buttonRow}>
            <Pressable
              style={({ pressed }) => [commonStyles.button, pressed && commonStyles.buttonPressed]}
              onPress={handleSubmit}
            >
              <Text style={commonStyles.buttonText}>Login</Text>
            </Pressable>
            <Link href="/register">New User?</Link>
          </View>
        </View>
      </View>
    </KeyboardAvoidingView>
  );
}
