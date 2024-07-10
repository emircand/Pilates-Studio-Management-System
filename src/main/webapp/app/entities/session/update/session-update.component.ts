import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IStaff } from 'app/entities/staff/staff.model';
import { StaffService } from 'app/entities/staff/service/staff.service';
import { IAthlete } from 'app/entities/athlete/athlete.model';
import { AthleteService } from 'app/entities/athlete/service/athlete.service';
import { SessionStatus } from 'app/entities/enumerations/session-status.model';
import { SessionService } from '../service/session.service';
import { ISession } from '../session.model';
import { SessionFormService, SessionFormGroup } from './session-form.service';

@Component({
  standalone: true,
  selector: 'jhi-session-update',
  templateUrl: './session-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class SessionUpdateComponent implements OnInit {
  isSaving = false;
  session: ISession | null = null;
  sessionStatusValues = Object.keys(SessionStatus);

  staffSharedCollection: IStaff[] = [];
  athletesSharedCollection: IAthlete[] = [];

  editForm: SessionFormGroup = this.sessionFormService.createSessionFormGroup();

  constructor(
    protected sessionService: SessionService,
    protected sessionFormService: SessionFormService,
    protected staffService: StaffService,
    protected athleteService: AthleteService,
    protected activatedRoute: ActivatedRoute,
  ) {}

  compareStaff = (o1: IStaff | null, o2: IStaff | null): boolean => this.staffService.compareStaff(o1, o2);

  compareAthlete = (o1: IAthlete | null, o2: IAthlete | null): boolean => this.athleteService.compareAthlete(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ session }) => {
      this.session = session;
      if (session) {
        this.updateForm(session);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const session = this.sessionFormService.getSession(this.editForm);
    if (session.id !== null) {
      this.subscribeToSaveResponse(this.sessionService.update(session));
    } else {
      this.subscribeToSaveResponse(this.sessionService.create(session));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ISession>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(session: ISession): void {
    this.session = session;
    this.sessionFormService.resetForm(this.editForm, session);

    this.staffSharedCollection = this.staffService.addStaffToCollectionIfMissing<IStaff>(this.staffSharedCollection, session.staff);
    this.athletesSharedCollection = this.athleteService.addAthleteToCollectionIfMissing<IAthlete>(
      this.athletesSharedCollection,
      session.athlete,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.staffService
      .query()
      .pipe(map((res: HttpResponse<IStaff[]>) => res.body ?? []))
      .pipe(map((staff: IStaff[]) => this.staffService.addStaffToCollectionIfMissing<IStaff>(staff, this.session?.staff)))
      .subscribe((staff: IStaff[]) => (this.staffSharedCollection = staff));

    this.athleteService
      .query()
      .pipe(map((res: HttpResponse<IAthlete[]>) => res.body ?? []))
      .pipe(map((athletes: IAthlete[]) => this.athleteService.addAthleteToCollectionIfMissing<IAthlete>(athletes, this.session?.athlete)))
      .subscribe((athletes: IAthlete[]) => (this.athletesSharedCollection = athletes));
  }
}
