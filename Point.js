import React, {useEffect, useState} from 'react';
import {StyleSheet, Text, View} from 'react-native';

export default function Point() {
  const [screenshot, setScreenshot] = useState(null);
  useEffect(async () => {}, []);

  return (
    <View style={styles.container}>
      <Text style={styles.hello}>{screenshot}</Text>
    </View>
  );
}

var styles = StyleSheet.create({
  container: {
    justifyContent: 'center',
    backgroundColor: 'blue',
    opacity: 0.5,
    width: 50,
    height: 50,
  },
  hello: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
});
