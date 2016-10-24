#!/bin/bash

activeP=5
totalP=10
inputFile="input.txt"

if [ $# -eq 4 ]
then
	if [ "$1" == "-p" ]
	then
	activeP=$2
	elif [ "$1" == "-n" ]
	then
	totalP=$2
	fi
	inputFile=$4

elif [ $# -eq 2 ]
then
	inputFile=$2

elif [ $# -eq 6 ]
then
	activeP=$2
	totalP=$4
	inputFile=$6
else
	echo "Invalid arguements"
	exit
fi

if [ $totalP -lt 3 ]
then
	echo "No of processes should be atleast 3"
	exit
fi

if [ $totalP -lt $activeP ]
then
	echo "No of processes should be atleast equal to active Processes"
	exit
fi


javac -sourcepath src -d bin src/**/**/**/*.java 


input="gnome-terminal"
start=0
i=1

while [ $i -le $activeP ]
do
	input=$input" --tab -e  \"bash -c 'java -cp bin: com.ds.gossip.MyServer $i $totalP -i $inputFile; exec bash' \" "
	i=$(($i+1))	
done

start=$i

while [ $start -le $totalP ]
do
	input=$input" --tab -e  \"bash -c 'java -cp bin: com.ds.gossip.MyServer $start $totalP ; exec bash' \" "
	start=$(($start+1))
done

#echo $activeP" "$totalP" "$inputFile

tmpfile=`mktemp`
echo $input > $tmpfile
chmod 744 $tmpfile
. $tmpfile
rm $tmpfile


