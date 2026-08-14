import { useState } from "react";
import { Link, useRouter } from "expo-router";
import {
  KeyboardAvoidingView,
  Platform,
  Pressable,
  Text,
  TextInput,
  View,
} from "react-native";
import { commonStyles } from "../styles/common";

export default function Register() {
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [errors, setErrors] = useState<string[]>([]);
  const url = `${process.env.EXPO_PUBLIC_API_URL}/register`;

  const handleSubmit = async () => {
    if (password !== confirmPassword) {
      setErrors(["Passwords do not match"]);
      return;
    }

    try {
      const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });
      if (response.status === 201) {
        router.replace("/login");
        return;
      }
      if (response.status === 400) {
        setErrors(await response.json());
        return;
      }
      console.error("Unexpected status:", response.status);
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <KeyboardAvoidingView
      style={{ flex: 1 }}
      behavior={Platform.OS === "ios" ? "padding" : "height"}
    >
      <View style={commonStyles.container}>
        <Text style={commonStyles.title}>Taskwave</Text>
        <Text style={commonStyles.subtitle}>Register:</Text>

        {errors.length > 0 && (
          <View style={commonStyles.errorBox}>
            {errors.map(e => <Text key={e} style={commonStyles.errorText}>{e}</Text>)}
          </View>
        )}

        <View style={commonStyles.form}>
          <Text>Email address:</Text>
          <TextInput
            style={commonStyles.input}
            value={email}
            onChangeText={setEmail}
            keyboardType="email-address"
            autoCapitalize="none"
          />
          <Text>Password:</Text>
          <TextInput
            style={commonStyles.input}
            value={password}
            onChangeText={setPassword}
            secureTextEntry
          />
          <Text>Confirm Password:</Text>
          <TextInput
            style={commonStyles.input}
            value={confirmPassword}
            onChangeText={setConfirmPassword}
            secureTextEntry
          />
          <View style={commonStyles.buttonRow}>
            <Pressable
              style={({ pressed }) => [commonStyles.button, pressed && commonStyles.buttonPressed]}
              onPress={handleSubmit}
            >
              <Text style={commonStyles.buttonText}>Register</Text>
            </Pressable>
            <Link href="/login">Returning User?</Link>
          </View>
        </View>
      </View>
    </KeyboardAvoidingView>
  );
}
