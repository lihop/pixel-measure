import {useEffect, useState} from 'react';

export default initialValue => {
  let _updaters = [];
  let _value = 'cyan';

  const useSingleton = () => {
    const [value, update] = useState(_value);

    useEffect(() => {
      _updaters.push(update);
      return () => (_updaters = _updaters.filter(el => el !== update));
    }, []);

    return value;
  };

  const updateSingleton = updateValue => {
    _value =
      typeof updateValue === 'function' ? updateValue(_value) : updateValue;
    _updaters.forEach(cb => cb(_value));
  };

  return [useSingleton, updateSingleton];
};
