import pandas as pd
import datetime as dt

def format_work_duration(stint):
        return {
                "start": stint.StartDate,
                "end": stint.EndDate,
                "detail": stint.Detail,
                "precision": stint.Precision,
                "activity": stint.Activity,
                "breaks": (stint.Breaks == "multiple")
        }

def us_date_to_iso_date(us_str):
        if str(us_str) == "nan":
                return ""
        try:
                us_date = dt.datetime.strptime(us_str, "%m/%d/%Y")
                return us_date.strftime("%Y-%m-%d")
        except ValueError:
                us_date = dt.datetime.strptime(us_str, "%m/%Y")
                return us_date.strftime("%Y-%m")
        

df = pd.read_csv("work-dates.csv", sep="@")

df["EndDate"] = df["EndDate"].apply(us_date_to_iso_date, convert_dtype=True)
df["StartDate"] = df["StartDate"].apply(us_date_to_iso_date, convert_dtype=True)
titles = df["WorkTitle"].unique()
for t in titles:
        title_obj = {
                "title": {
                        "ru": t
                }
        }
        flat_work_pds = df[df["WorkTitle"] == t]
        title_obj["pub"] = flat_work_pds["Publication"].iloc[0]
        title_obj["title"]["en"] = flat_work_pds["EnglishTitle"].iloc[0]
        title_obj["work"] = [format_work_duration(row) for row in flat_work_pds.itertuples()]
        print(title_obj)