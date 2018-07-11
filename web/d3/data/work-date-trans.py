import pandas as pd
import datetime as dt
from json import dumps
import itertools

def format_work_duration(stint):
        return {
                "start": stint.StartDate,
                "end": stint.EndDate,
                "detail": stint.Detail,
                "precision": stint.Precision,
                "activity": stint.Activity,
                "breaks": (stint.Breaks == "multiple")
        }

def format_by_title(ru_title, df):
        title_obj = {
                "title": {
                        "ru": ru_title
                }
        }
        flat_work_pds = df[df["WorkTitle"] == ru_title]
        title_obj["pub"] = flat_work_pds["Publication"].iloc[0]
        title_obj["title"]["en"] = flat_work_pds["EnglishTitle"].iloc[0]
        title_obj["work"] = [format_work_duration(row) for row in flat_work_pds.itertuples()]
        title_obj["max"] = max(flat_work_pds["EndDate"])
        title_obj["min"] = min(flat_work_pds["StartDate"])
        return title_obj

#figure out what's overlapping when to determine what row it should be promoted to
def add_row_number(works):
        intervals = list([{"title": w["title"]["ru"], "date": w["max"], "type": "close"} for w in works])
        intervals += list([{"title": w["title"]["ru"], "date": w["min"], "type": "open"} for w in works])
        intervals.sort(key=(lambda x: (x["date"], x["type"] == "close")))
        open_works = {}
        closed_works = {}
        for endpoint in intervals:
                if endpoint["type"] == "open":
                        row_assm = open_works.values()
                        if len(open_works) == 0:
                                open_works[endpoint["title"]] = 0
                        else:
                                for i in itertools.count():
                                        if i not in row_assm:
                                                open_works[endpoint["title"]] = i
                                                break
                        
                else:
                        closed_works[endpoint["title"]] = open_works[endpoint["title"]]
                        open_works[endpoint["title"]] = -1

        for i, row in enumerate(works):
                display_row = closed_works[works[i]["title"]["ru"]]
                works[i]["display_row"] = display_row
        
        return works

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
df.fillna("", inplace=True)

out = list([format_by_title(t, df) for t in titles])
print(dumps(add_row_number(out)))