import { useRouter } from "expo-router";
import { useState } from "react";
import { Button, Image, Pressable, StyleSheet, Text, View } from "react-native";
import { useAuth } from "../../contexts/AuthContext";
import { createMaterialTopTabNavigator } from '@react-navigation/material-top-tabs';
import { SafeAreaView } from "react-native-safe-area-context";
import Not_Started from "./not-started";
import In_Progress from "./in-progress";
import Finished from "./finished";

const Tab = createMaterialTopTabNavigator();

export default function TabLayout() {
  const router = useRouter();
  const { token, setToken } = useAuth();
  const ICON_SIZE = 50;
  const titles: Record<string, string> = {
    "not-started": "Not Started",
    "in-progress": "In Progress",
    "finished": "Finished",
  };
  const [title, setTitle] = useState("Not Started");
  const NULL_TASK = {
    task: JSON.stringify({
      taskId: 0,
      userId: 0,
      title: "",
      description: "",
      status: "NOT_STARTED",
      hours: 0,
      minutes: 0
    })
  };

  const handleLogout = () => {
    setToken(null);
  };

  return (
    <SafeAreaView style={{ flex: 1 }} >
      <View style={{ flexDirection: "row-reverse", gap: 8, padding: 12}}>
        {token
          ? <Button title="Logout" onPress={handleLogout} />
          : <Button title="Sign In" onPress={() => router.push("/login")} />
        }
        <Pressable
          style={styles.addButton}
          onPress={() => router.push({
            pathname: "/task-form",
            params: NULL_TASK
          })}
        >
          <Text style={styles.addButtonText}>+</Text>
        </Pressable>
        <Text style={styles.titleText}>{title}</Text>
      </View>
      <Tab.Navigator
        tabBarPosition="bottom"
        screenListeners={({ route }) => ({
          focus: () => setTitle(titles[route.name]),
        })}
      >
        <Tab.Screen name="not-started" component={Not_Started} options={{ title: "Not Started", tabBarIcon: () => <Image source={require("../../assets/images/not-started.png")} style={{ width: ICON_SIZE, height: ICON_SIZE }} /> }} />
        <Tab.Screen name="in-progress" component={In_Progress} options={{ title: "In Progress", tabBarIcon: () => <Image source={require("../../assets/images/in-progress.png")} style={{ width: ICON_SIZE, height: ICON_SIZE }} /> }} />
        <Tab.Screen name="finished" component={Finished} options={{ title: "Finished", tabBarIcon: () => <Image source={require("../../assets/images/completed.png")} style={{ width: ICON_SIZE, height: ICON_SIZE }} /> }} />
      </Tab.Navigator>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  addButton: {
    width: 36,
    height: 36,
    borderRadius: 18,
    backgroundColor: "#06b6d4",
    justifyContent: "center",
    alignItems: "center",
    marginRight: 8,
  },
  addButtonText: {
    color: "white",
    fontSize: 24,
    lineHeight: 28,
    fontWeight: "300",
  },
  titleText: {
    fontSize: 24,
    fontWeight: "300",
    marginRight: "auto"
  }
});
