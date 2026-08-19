import { Pressable, StyleSheet, Text, useWindowDimensions, View } from "react-native";
import { useAuth } from "../contexts/AuthContext";
import { useRouter } from "expo-router";
import { updateTask, deleteTask } from "../lib/db";
import Animated, {
  useSharedValue,
  useAnimatedStyle,
  withTiming,
} from "react-native-reanimated";
import { scheduleOnRN } from "react-native-worklets";
import { getNextStatus } from "../lib/taskStatus";
import { commonStyles } from "../styles/common";

export interface Task {
  taskId: number;
  userId: number;
  title: string;
  description: string;
  status: string;
  hours: number;
  minutes: number;
}

export default function TaskCard({
  task,
  onDelete,
  onStatusChange,
}: {
  task: Task;
  onDelete: (taskId: number) => void;
  onStatusChange: (updatedTask: Task) => void;
}) {
  const time = task.hours !== 0
    ? `${task.hours} hours`
    : `${task.minutes} minutes`;

  const { token } = useAuth();
  const { width } = useWindowDimensions();
  const router = useRouter();
  const translateX = useSharedValue(0);
  const animatedStyle = useAnimatedStyle(() => ({
    transform: [{ translateX: translateX.value }],
  }));

  const slideOut = (direction: "left" | "right", callback: () => void) => {
    // include callback to order status edits after animations
    // https://docs.swmansion.com/react-native-reanimated/docs/2.x/: animations are asynchronous
    translateX.value = withTiming(
      direction === "left" ? -width : width,
      { duration: 250 },
      (finished) => {
        if (finished) scheduleOnRN(callback);
      }
    );
  };

  const handleStatusChange = async (forward: boolean) => {
    const nextStatus = getNextStatus(task.status, forward);
    if (!nextStatus) return;

    const updatedTask = { ...task, status: nextStatus };
    try {
      if (!token) {
        // local update
        await updateTask(updatedTask);
      } else {
        // server update
        const response = await fetch(
          `${process.env.EXPO_PUBLIC_API_URL}/task/update/${task.taskId}`,
          {
            method: "PUT",
            headers: {
              "Content-Type": "application/json",
              "Authorization": `Bearer ${token}`,
            },
            body: JSON.stringify(updatedTask),
          }
        );
        if (response.status !== 204) {
          console.error("Unexpected status:", response.status);
          return;
        }
      }
      slideOut(forward ? "right" : "left", () => onStatusChange(updatedTask));
    } catch (e) {
      console.error(e);
    }
  };

  const handleEditTask = () => {
    router.push({
      pathname: "/task-form",
      params: { task: JSON.stringify(task) }
    })
  }

  const handleDelete = async () => {
    try {
      if (!token) {
        // local delete
        await deleteTask(task.taskId);
      } else {
        // server delete
        const response = await fetch(
          `${process.env.EXPO_PUBLIC_API_URL}/task/delete/${task.taskId}`,
          {
            method: "DELETE",
            headers: { "Authorization": `Bearer ${token}` },
          }
        );
        if (response.status === 404) {
          console.error("Task not found:", task.taskId);
          return;
        }
        if (response.status !== 204) {
          console.error("Unexpected status:", response.status);
          return;
        }
      }
      onDelete(task.taskId);
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <Animated.View style={[styles.cardDimensions, animatedStyle]}>
      <Pressable style={({ pressed }) => [styles.card, pressed && styles.cardPressed]} onLongPress={handleEditTask}>
        <Text style={styles.title}>{task.title}</Text>
        <Text style={styles.notes}>{task.description}</Text>
        <Text style={styles.time}>Time: {time}</Text>
        <View style={styles.buttonRow}>
          <Pressable style={styles.button} onPress={() => handleStatusChange(false)}>
            <Text style={styles.buttonText}>←</Text>
          </Pressable>
          <Pressable style={styles.button} onPress={() => handleStatusChange(true)}>
            <Text style={styles.buttonText}>→</Text>
          </Pressable>
          <Pressable
            style={({ pressed }) => [styles.button, commonStyles.deleteButton, pressed && commonStyles.deleteButtonPressed]}
            onPress={handleDelete}>
            <Text style={[commonStyles.deleteButtonText]}>x</Text>
          </Pressable>
        </View>
      </Pressable>
    </Animated.View>
  );
}

const styles = StyleSheet.create({
  cardDimensions: {
    marginVertical: 6,
    marginHorizontal: 12,
  },
  card: {
    backgroundColor: "rgba(134, 210, 255, 0.8)",
    boxShadow: "0px 0px 10px rgb(134, 210, 255)",
    borderRadius: 12,
    padding: 12,
  },
  cardPressed: {
    backgroundColor: "rgba(174, 225, 255, 0.8)",
    boxShadow: "0px",
  },
  title: {
    fontSize: 18,
    fontWeight: "600",
    textAlign: "center",
    marginBottom: 8,
  },
  notes: {
    fontSize: 14,
    marginBottom: 4,
  },
  time: {
    fontSize: 14,
    marginBottom: 8,
  },
  buttonRow: {
    flexDirection: "row",
    gap: 8,
  },
  button: {
    borderRadius: 12,
    paddingHorizontal: 12,
    paddingVertical: 6,
  },
  buttonText: {
    fontWeight: "bold",
  },
});