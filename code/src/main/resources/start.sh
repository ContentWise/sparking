# $1: agent name
# $2: config file
# $3: logging level (INFO default)
LOGGING="INFO"

if [ ! -z $3 ] 
then
   LOGGING=$3
fi

flume-ng agent -n $1 --conf /opt/flume/conf -f $2 -Dflume.root.logger=$LOGGING,console
