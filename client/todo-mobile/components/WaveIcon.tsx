import Svg, { G, Path } from "react-native-svg";

const WAVE_PATH = "M2969 3855 c-435 -73 -753 -357 -903 -806 -85 -255 -116 -467 -123 -845 l-5 -291 27 -21 c36 -28 70 -28 99 1 23 23 24 27 29 308 16 877 268 1340 807 1487 135 37 498 39 685 4 266 -50 718 -190 790 -245 17 -13 -7 -14 -220 -10 -892 19 -1219 -321 -979 -1018 55 -161 176 -401 222 -439 191 -159 533 -190 945 -85 195 50 232 75 207 139 -22 59 -47 62 -187 22 -196 -54 -291 -69 -448 -70 -169 -1 -238 12 -348 68 l-81 41 -53 105 c-216 429 -240 737 -69 903 141 138 367 189 820 186 284 -2 326 4 369 50 86 93 -17 215 -260 307 -482 184 -994 265 -1324 209z";

interface WaveIconProps {
  size?: number;
  color?: string;
  strokeWidth?: number;
  mirrored?: boolean;
}

export function WaveIcon({ size = 24, color = "rgb(75, 150, 255)", strokeWidth = 0, mirrored = false }: WaveIconProps) {
  return (
    <Svg
      width={size}
      height={size}
      viewBox="150 150 350 350"
      style={mirrored ? { transform: [{ scaleX: -1 }] } : undefined}
    >
      <G transform="translate(0,600) scale(0.1,-0.1)" fill={color}>
        <Path d={WAVE_PATH} stroke={color} strokeWidth={strokeWidth} strokeLinejoin="round" />
      </G>
    </Svg>
  );
}
